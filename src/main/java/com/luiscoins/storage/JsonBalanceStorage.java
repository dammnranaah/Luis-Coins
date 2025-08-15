package com.luiscoins.storage;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonBalanceStorage implements BalanceStorage {
    private final File file;
    private Map<String, Object> data = new HashMap<>();

    public JsonBalanceStorage(File file) {
        this.file = file;
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                String json = sb.toString().trim();
                if (!json.isEmpty()) data = parse(json);
            } catch (IOException ignored) {}
        }
    }

    @Override
    public Double load(UUID uuid) {
        Object o = getNested("balances." + uuid.toString());
        if (o instanceof Number) return ((Number)o).doubleValue();
        return null;
    }

    @Override
    public void save(UUID uuid, double balance) {
        setNested("balances." + uuid.toString(), balance);
    }

    @Override
    public Long loadCooldown(UUID uuid) {
        Object o = getNested("cooldowns." + uuid.toString());
        if (o instanceof Number) return ((Number)o).longValue();
        return null;
    }

    @Override
    public void saveCooldown(UUID uuid, long timestamp) {
        setNested("cooldowns." + uuid.toString(), timestamp);
    }

    @Override
    public void flush() {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(stringify(data));
        } catch (IOException ignored) {}
    }

    private static String stringify(Object obj) {
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?,?> e : ((Map<?,?>) obj).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':');
                sb.append(stringify(e.getValue()));
            }
            sb.append('}');
            return sb.toString();
        } else if (obj instanceof String) {
            return '"' + escape((String) obj) + '"';
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return String.valueOf(obj);
        } else if (obj == null) {
            return "null";
        }
        return '"' + escape(String.valueOf(obj)) + '"';
    }

    private static String escape(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parse(String json) {
        Parser p = new Parser(json);
        return p.parseObject();
    }

    private Object getNested(String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> cur = data;
        for (int i=0;i<parts.length;i++) {
            String k = parts[i];
            if (i==parts.length-1) return cur.get(k);
            Object nxt = cur.get(k);
            if (!(nxt instanceof Map)) return null;
            cur = (Map<String, Object>) nxt;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void setNested(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> cur = data;
        for (int i=0;i<parts.length;i++) {
            String k = parts[i];
            if (i==parts.length-1) {
                cur.put(k, value);
            } else {
                Object nxt = cur.get(k);
                if (!(nxt instanceof Map)) {
                    nxt = new HashMap<String, Object>();
                    cur.put(k, nxt);
                }
                cur = (Map<String, Object>) nxt;
            }
        }
    }

    private static class Parser {
        private final String s;
        private int i=0;
        Parser(String s){this.s=s;}
        Map<String,Object> parseObject(){
            skip();
            if (s.charAt(i)!='{') return new HashMap<>();
            i++;
            Map<String,Object> m=new HashMap<>();
            skip();
            while (i<s.length() && s.charAt(i)!='}'){
                skip();
                String key=parseString();
                skip();
                if (i<s.length() && s.charAt(i)==':') i++;
                skip();
                Object val=parseValue();
                m.put(key,val);
                skip();
                if (i<s.length() && s.charAt(i)==',') i++;
                skip();
            }
            if (i<s.length()&&s.charAt(i)=='}') i++;
            return m;
        }
        Object parseValue(){
            skip();
            if (i>=s.length()) return null;
            char c=s.charAt(i);
            if (c=='"') return parseString();
            if (c=='{') return parseObject();
            if (c=='t'&&s.startsWith("true",i)){i+=4;return Boolean.TRUE;}
            if (c=='f'&&s.startsWith("false",i)){i+=5;return Boolean.FALSE;}
            if (c=='n'&&s.startsWith("null",i)){i+=4;return null;}
            int j=i;
            while (i<s.length() && "-+.0123456789".indexOf(s.charAt(i))>=0) i++;
            String num=s.substring(j,i);
            try{
                if (num.contains(".")||num.contains("e")||num.contains("E")) return Double.parseDouble(num);
                return Long.parseLong(num);
            }catch(Exception e){return 0;}
        }
        String parseString(){
            if (s.charAt(i)!='"') return "";
            i++;
            StringBuilder sb=new StringBuilder();
            while (i<s.length()&&s.charAt(i)!='"'){
                char c=s.charAt(i++);
                if (c=='\\' && i<s.length()){
                    char n=s.charAt(i++);
                    if (n=='"'||n=='\\') sb.append(n);
                    else if (n=='n') sb.append('\n');
                    else if (n=='t') sb.append('\t');
                    else sb.append(n);
                } else sb.append(c);
            }
            if (i<s.length()&&s.charAt(i)=='"') i++;
            return sb.toString();
        }
        void skip(){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;}
    }
}
