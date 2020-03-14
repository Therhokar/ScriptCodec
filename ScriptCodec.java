import java.nio.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

public class ScriptCodec {

    static class Numbers {
        static final int MAP_SIZE = 310;
        static HashMap<String,String> numbers;
    }

    public static List<String> transform(String file_name) {
        Path input = Paths.get(file_name)
            .toAbsolutePath();
        Charset charset = Charset.forName("UTF-8");
        List<String> script = new LinkedList<String>();
        int index = 0;

        try (BufferedReader reader = Files.newBufferedReader(input, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                script.add(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        List<String> templated_script = new LinkedList<String>();
        for (String s : script) {
            if (s.contains("<Speaker>")) {
                int spkr_elmt = s.indexOf("<Speaker>");
                int spkr_elmt_end = s.indexOf("</Speaker>");
                int words_index = s.indexOf(':');

                String speaker = s.substring(spkr_elmt + 9,
                    spkr_elmt_end);
                String words = s.substring(words_index + 1);
                String pic = "";
                if (s.contains("NPC-Kalin")) {
                    pic = "0_kalina".concat(s.substring(s.indexOf('(') + 1, s.indexOf(')')));
                } else
                    pic = s.substring(0, s.indexOf('('));
                if (matchNumber(pic) != null)
                    pic = matchNumber(pic);
                String new_s;
                if (speaker.isEmpty()) {
                    new_s = words.replace("+", "<br>");
                } else {
                    new_s = "{{剧情对话行|"
                        .concat("name=")
                        .concat(speaker)
                        .concat("|自定图片=")
                        .concat(pic)
                        .concat("|text=")
                        .concat(words)
                        .concat("}}")
                        .replace("<color=", "{{Color|")
                        .replace("</color>", "}}")
                        .replace('>', '|')
                        .replace("+", "<br>\n");
                }
                templated_script.add(new_s.replace("+", "<br>\n"));
            } else {
                if (s.indexOf(':') != s.length() - 1)
                    templated_script.add(s.substring(s.indexOf(':') + 1).replace("+", "<br>").concat("<br>"));
            }
        }

        return templated_script;
    }

    private static String matchNumber(String name) {
        
        return Numbers.numbers.get(name.replace(" ", "_"));
    }

    public static void main(String[] args) {
        Numbers.numbers = new HashMap<String,String>(Numbers.MAP_SIZE);
        Path map = Paths.get("./map.txt").toAbsolutePath();
        Charset charset = Charset.forName("UTF-8");

        try (BufferedReader reader = Files.newBufferedReader(map, charset)) {
            String pair = null;
            while ((pair = reader.readLine()) != null) {
                String[] splitPair = pair.split(" ");
                Numbers.numbers.put(splitPair[0], splitPair[1]);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        List<String> script = transform(args[0]);
        Path output = Paths.get(args[1])
            .toAbsolutePath();
        try (BufferedWriter writer = Files.newBufferedWriter(output, charset)) {
            for (String s : script) {
            writer.write(s, 0, s.length());
            writer.newLine();
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }
}
