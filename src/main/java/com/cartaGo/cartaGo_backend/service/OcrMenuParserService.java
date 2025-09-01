package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.PlatoRequestDTO;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;

/**
 * Parser de texto OCR de una carta -> lista de PlatoRequestDTO
 *
 * Reglas:
 * - Sección: líneas que terminan en ":" (o variantes OCR) o coinciden EXACTAS con keywords o "header-like".
 * - Precio: detecta "12€", "12 EUR", "5,9€", "13.50 €" (coma o punto, espacios NBSP).
 * - Nombre: mismo renglón que el precio o renglón anterior.
 * - Descripción: la 1ª línea tras el precio, salvo que huela a nombre de plato.
 * - Si no se detecta sección, se asigna "General".
 */
public class OcrMenuParserService {

    private static final String DEFAULT_SECTION = "General";
    private static final boolean DEBUG_TRACE = false;

    // Palabras clave para detectar secciones (comparación en MAYÚSCULAS y sin tildes)
    private static final Set<String> SECTION_KEYWORDS = new HashSet<>(Arrays.asList(
            // generales
            "MENU","MENÚ","MENU DEL DIA","MENÚ DEL DÍA","PRIMEROS","SEGUNDOS","PLATOS COMBINADOS",
            "ESPECIALIDADES","ESPECIALIDADES DE LA CASA","SUGERENCIAS","RECOMENDACIONES","DEL CHEF","FAVORITOS",
            // entrantes / tapas
            "ENTRANTES","ENTRANTE","APERITIVOS","PICOTEO","TAPAS","RACIONES","ENTRADA",
            // carnes / pescados
            "CARNES","CARNE","CARNES A LA BRASA","ASADOS","GRILL","PARRILLA",
            "PESCADOS","PESCADO","MARISCOS","MARISCO",
            // ensaladas / verduras
            "ENSALADAS","ENSALADA","ENSALADILLAS","VEGETALES","VERDURAS",
            // internacionales
            "POKES","POKE","HAMBURGUESAS","HAMBURGUESA","PIZZAS","PASTAS","PASTA","BURRITOS","TACOS","QUESADILLAS",
            "SUSHI","NIGIRI","MAKI","URAMAKI","TEMPURA","BOWLS","RAMEN","WOK",
            // bebidas
            "BEBIDAS","BEBIDA","VINOS","COPAS","COCTELES","CÓCTELES","CAFES","CAFÉS","INFUSIONES","ZUMOS",
            // postres / dulces
            "POSTRES","POSTRE","HELADOS","DULCES","REPOSTERIA","REPOSTERÍA","GOFRES","CREPES","TARTAS"
    ));

    // ====== Regex robusta de precios (soporta NBSP y "EUR") ======
    private static final String PRICE_CORE = "(\\d{1,4}(?:[.,]\\d{1,2})?)";
    private static final String EUR_SIGN = "(?:€|EUR)";
    private static final String HSPACE = "[ \\t\\u00A0\\u2007\\u202F]*"; // espacios normales + NBSPs
    private static final java.util.regex.Pattern PRICE_EXTRACT =
            java.util.regex.Pattern.compile(PRICE_CORE + HSPACE + EUR_SIGN);
    private static final java.util.regex.Pattern PRICE_ANYWHERE =
            java.util.regex.Pattern.compile(".*" + PRICE_CORE + HSPACE + EUR_SIGN + ".*");

    /** Convierte un bloque de texto OCR en platos */
    public List<PlatoRequestDTO> parse(String fullText) {
        List<PlatoRequestDTO> platos = new ArrayList<>();
        if (fullText == null || fullText.isBlank()) return platos;

        String currentSection = null;
        String[] rawLines = fullText.replace("\r", "").split("\n");

        String pendingName = null; // candidato a nombre esperando precio
        int descAllowance = 0;     // nº de líneas de descripción permitidas tras un precio (1)
        int emptyRun = 0;          // líneas vacías consecutivas

        for (int i = 0; i < rawLines.length; i++) {
            String line = normalizeBasic(rawLines[i]);
            trace("LINE", line);

            // --- look-ahead: próxima línea NO vacía ---
            String nextNonEmpty = null;
            for (int j = i + 1; j < rawLines.length; j++) {
                String cand = normalizeBasic(rawLines[j]);
                if (!cand.isBlank()) { nextNonEmpty = cand; break; }
            }
            boolean nextIsPrice = (nextNonEmpty != null) && containsPrice(nextNonEmpty);

            // ——— LÍNEA VACÍA ———
            if (line.isBlank()) {
                emptyRun++;
                if (pendingName != null && !platos.isEmpty()) {
                    PlatoRequestDTO last = platos.get(platos.size() - 1);
                    last.setDescripcion(mergeDesc(last.getDescripcion(), pendingName));
                    trace("DESC_FROM_PENDING_ON_BLANK", pendingName);
                    pendingName = null;
                }
                continue;
            }

            // ——— NUEVO BLOQUE: tras vacías, ¿es sección? ———
            if (emptyRun >= 1 && isSection(line)) {
                if (pendingName != null && !platos.isEmpty()) {
                    PlatoRequestDTO last = platos.get(platos.size() - 1);
                    last.setDescripcion(mergeDesc(last.getDescripcion(), pendingName));
                    trace("DESC_FROM_PENDING_BEFORE_SECTION", pendingName);
                    pendingName = null;
                }
                trace("SECTION(blk)", stripColonLike(line));
                currentSection = stripColonLike(line);
                descAllowance = 0;
                emptyRun = 0;
                continue;
            }
            emptyRun = 0;

            // ——— SECCIÓN EXPLÍCITA ———
            if (isSection(line)) {
                if (pendingName != null && !platos.isEmpty()) {
                    PlatoRequestDTO last = platos.get(platos.size() - 1);
                    last.setDescripcion(mergeDesc(last.getDescripcion(), pendingName));
                    trace("DESC_FROM_PENDING_BEFORE_SECTION", pendingName);
                    pendingName = null;
                }
                trace("SECTION", stripColonLike(line));
                currentSection = stripColonLike(line);
                descAllowance = 0;
                continue;
            }

            // ——— NOMBRE + PRECIO EN MISMA LÍNEA ———
            if (containsPrice(line) && hasNameBesidePrice(line)) {
                trace("NAME+PRICE", line);
                PlatoRequestDTO plato = buildFromSameLine(line, currentSection);
                platos.add(plato);
                pendingName = null;
                descAllowance = 1; // permitir 1 línea de descripción
                continue;
            }

            // ——— PRECIO EN LÍNEA SEPARADA ———
            if (containsPrice(line)) {
                trace("PRICE", line);
                if (pendingName != null) {
                    PlatoRequestDTO plato = buildFromPrevNameAndPrice(pendingName, line, currentSection);
                    platos.add(plato);
                    pendingName = null;
                    descAllowance = 1; // permitir 1 línea de descripción
                } else {
                    trace("CANDIDATE/NAME?", line); // precio sin nombre claro → ignoramos
                }
                continue;
            }

            // ——— PRIMERA LÍNEA TRAS PRECIO ———
            if (descAllowance > 0 && !platos.isEmpty()) {
                // Regla principal con look-ahead:
                // - Si la siguiente NO vacía es PRECIO -> ESTA línea es NOMBRE del siguiente plato
                // - Si ESTA línea empieza por preposición típica -> DESCRIPCIÓN
                // - Si ESTA línea "huele" a nombre y además la siguiente es PRECIO -> NOMBRE
                // - En caso de duda y sin precio detrás -> DESCRIPCIÓN
                String up = normalizeForCompare(line);
                boolean startsWithPrep = up.startsWith("CON ") || up.startsWith("DE ") || up.startsWith("AL ")
                        || up.startsWith("A LA ") || up.startsWith("A LOS ") || up.startsWith("A LAS ")
                        || up.startsWith("EN ");

                if (nextIsPrice || (isProbablyDishName(line) && nextIsPrice)) {
                    pendingName = line;
                    descAllowance = 0;
                    trace("NAME_AFTER_PRICE", line);
                    continue;
                } else if (startsWithPrep || isProbablyDescription(line)) {
                    PlatoRequestDTO last = platos.get(platos.size() - 1);
                    last.setDescripcion(mergeDesc(last.getDescripcion(), line));
                    descAllowance = 0;
                    trace("DESC", line);
                    continue;
                } else {
                    PlatoRequestDTO last = platos.get(platos.size() - 1);
                    last.setDescripcion(mergeDesc(last.getDescripcion(), line));
                    descAllowance = 0;
                    trace("DESC_FALLBACK", line);
                    continue;
                }
            }

            // ——— CANDIDATO A NOMBRE (a la espera de precio) ———
            pendingName = line;
            trace("NAME_CANDIDATE", line);
        }

        // ——— FIN DEL TEXTO: si queda pendingName y parece descripción, añadirla ———
        if (pendingName != null && !platos.isEmpty() && isProbablyDescription(pendingName)) {
            PlatoRequestDTO last = platos.get(platos.size() - 1);
            last.setDescripcion(mergeDesc(last.getDescripcion(), pendingName));
            trace("DESC_FROM_PENDING_AT_END", pendingName);
        }

        // sección por defecto si faltase
        for (PlatoRequestDTO p : platos) {
            if (p.getSeccion() == null || p.getSeccion().isBlank()) {
                p.setSeccion(DEFAULT_SECTION);
            }
        }
        return platos;
    }

    // =================== helpers ===================

    private boolean isSection(String line) {
        if (containsPrice(line)) return false;
        String stripped = stripColonLike(line);
        if (stripped.isBlank()) return false;
        if (!line.equals(stripped)) return true; // terminaba con "colon-like" => sección
        String up = normalizeForCompare(stripped);
        if (SECTION_KEYWORDS.contains(up)) return true; // keyword exacta
        return isHeaderLike(stripped); // heurística
    }

    // Quita : y variantes OCR al final (： ﹕ ;) + espacios raros
    private String stripColonLike(String line) {
        String s = normalizeBasic(line);
        s = s.replaceAll("[\\s\\u00A0\\u2007\\u202F]*[:：﹕;]+\\s*$", "");
        return s.trim();
    }

    private boolean isHeaderLike(String line) {
        String s = line.trim();
        if (s.length() > 24) return false;
        if (s.matches(".*[0-9€].*")) return false;
        if (s.endsWith(".") || s.startsWith("-") || s.startsWith("•")) return false;

        String[] words = s.split("\\s+");
        if (words.length == 0 || words.length > 4) return false;

        String up = normalizeForCompare(s);
        if (up.contains(" DE ") || up.contains(" CON ") || up.contains(" A LA ") ||
                up.contains(" AL ") || up.contains(" EN ")) return false;

        int letters = 0, uppers = 0;
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) uppers++;
            }
        }
        if (letters == 0) return false;
        double upperRatio = (double) uppers / letters;
        return upperRatio >= 0.75;
    }

    private boolean containsPrice(String line) { return PRICE_ANYWHERE.matcher(line).matches(); }

    private String extractPrice(String line) {
        var m = PRICE_EXTRACT.matcher(line);
        return m.find() ? m.group(1) : null;
    }

    private boolean hasNameBesidePrice(String line) {
        String withoutPrice = PRICE_EXTRACT.matcher(line).replaceAll("").trim();
        return !withoutPrice.isBlank();
    }

    private PlatoRequestDTO buildFromSameLine(String line, String currentSection) {
        String priceStr = extractPrice(line);
        BigDecimal precio = toBigDecimal(priceStr);
        String nombre = PRICE_EXTRACT.matcher(line).replaceAll("").trim();

        PlatoRequestDTO dto = base(currentSection);
        dto.setNombre(nombre);
        dto.setPrecio(precio);
        return dto;
    }

    private PlatoRequestDTO buildFromPrevNameAndPrice(String prevName, String priceLine, String currentSection) {
        String priceStr = extractPrice(priceLine);
        BigDecimal precio = toBigDecimal(priceStr);

        PlatoRequestDTO dto = base(currentSection);
        dto.setNombre(prevName);
        dto.setPrecio(precio);
        return dto;
    }

    private PlatoRequestDTO base(String currentSection) {
        PlatoRequestDTO dto = new PlatoRequestDTO();
        dto.setSeccion(currentSection);
        dto.setDescripcion(null);
        dto.setOrden(null);
        dto.setImagen(null);
        dto.setAlergenosIds(null);
        return dto;
    }

    private BigDecimal toBigDecimal(String price) {
        if (price == null) return null;
        String normalized = price.replace(",", ".");
        try { return new BigDecimal(normalized); }
        catch (NumberFormatException e) { return null; }
    }

    private String mergeDesc(String current, String add) {
        if (current == null || current.isBlank()) return add;
        return current + " " + add;
    }

    private String normalizeBasic(String s) {
        if (s == null) return "";
        // Cambia NBSP (U+00A0) y otros espacios “raros” por espacio normal
        return s.replace('\u00A0', ' ')
                .replace('\u2007', ' ')
                .replace('\u202F', ' ')
                .trim();
    }

    private String normalizeForCompare(String in) {
        if (in == null) return "";
        String noAccents = Normalizer.normalize(in, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", ""); // quita acentos
        return noAccents.trim().toUpperCase();
    }

    private void trace(String tag, String line) {
        if (!DEBUG_TRACE) return;
        System.out.println(String.format("[OCR-PARSE][%s] «%s»", tag, line));
    }

    // ¿Parece un nombre de plato?
    private boolean isProbablyDishName(String line) {
        String s = line.trim();
        if (s.isEmpty()) return false;
        if (containsPrice(s) || isSection(s)) return false;

        String up = normalizeForCompare(s);

        // Si empieza por preposición típica -> NO es nombre (suele ser descripción)
        String[] badStarts = {"CON ", "DE ", "AL ", "A LA ", "A LOS ", "A LAS ", "EN "};
        for (String bs : badStarts) if (up.startsWith(bs)) return false;

        // Prefijos típicos de nombres
        String[] dishPrefixes = {
                "POKE ", "PIZZA ", "HAMBURGUESA", "HAMBURGUESAS", "ENSALADA", "ENSALADAS",
                "TACO", "TACOS", "BURRITO", "BURRITOS", "RAMEN", "SUSHI", "NIGIRI", "MAKI",
                "URAMAKI", "TEMPURA", "BOWL", "WOK", "CREPE", "GOFRE", "TARTA", "HELADO"
        };
        for (String pref : dishPrefixes) if (up.startsWith(pref)) return true;

        // “Nombre de/con/a la/al/en …”
        if (up.matches("^[A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑa-záéíóúñ]+\\s+(DE|CON|A LA|AL|EN)\\s+.*")) return true;

        // 2–6 palabras, inicia en mayúscula, no termina en punto → posible título de plato
        String[] words = s.split("\\s+");
        return (words.length >= 2 && words.length <= 6 &&
                Character.isUpperCase(s.codePointAt(0)) &&
                !s.endsWith("."));
    }

    // ¿Parece una descripción?
    private boolean isProbablyDescription(String line) {
        String s = line.trim();
        if (s.isEmpty()) return false;

        // Nunca descripción si parece sección, contiene precio o parece nombre
        if (isSection(s) || containsPrice(s) || isProbablyDishName(s)) return false;

        String up = normalizeForCompare(s);

        // sufijos típicos de unidades
        if (up.endsWith(" UNIDAD") || up.endsWith(" UNIDADES") || up.endsWith(" UDS") ||
                up.endsWith(" RACION")  || up.endsWith(" RACIONES") || up.endsWith(" PIEZA") ||
                up.endsWith(" PIEZAS")) return true;

        // señales de frase descriptiva
        if (s.contains(",") || s.contains("...") || s.endsWith(".")) return true;

        // patrones de preparación comunes
        if (up.matches(".*\\b(AL HORNO|A LA PLANCHA|MARINADO|EMPANADO|A LA BRASA|CON.*|DE.*)\\b.*")) return true;

        return false;
    }
}
