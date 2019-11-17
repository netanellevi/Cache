import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository that demonstrate the library functionality
 */
public class DemoRepo implements RepositoryProvider {
    private BufferedReader br;
    private BufferedWriter bw;
    private FileInputStream fIn;
    private String filepath;

    DemoRepo(String filepath) {
        this.filepath = filepath;
        try {
            fIn = new FileInputStream(filepath);
            this.br = new BufferedReader(new InputStreamReader(fIn));
            this.bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filepath, true), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Error loading DemoRepo");
        }
    }

    @Override
    public JSONObject get(Integer eId) {
        try {
            fIn.getChannel().position(0);
            br = new BufferedReader(new InputStreamReader(fIn));
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject jsonObject = new JSONObject(line);
                if ((int) jsonObject.get("id") == eId) {
                    return jsonObject;
                }
            }
        } catch (JSONException | IOException ignored) {
        }
        return null; //not found or failed to provide
    }

    @Override
    public ReturnSate add(JSONObject jsonObject) {
        try {
            if (get((int) jsonObject.get("id")) == null) {
                try {
                    bw.write(jsonObject.toString() + "\n");
                    bw.flush();
                } catch (IOException e) {
                    return ReturnSate.FAILURE;
                }

            } else return ReturnSate.ALREADY_EXISTS;
        } catch (JSONException c) {
            return ReturnSate.FAILURE;
        }
        return ReturnSate.SUCCESS;
    }

    @Override
    public ReturnSate update(JSONObject updated) {
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(Path.of(filepath), StandardCharsets.UTF_8));
            int i;
            for (i = 0; i < fileContent.size(); i++) {
                JSONObject old = new JSONObject(fileContent.get(i));
                if ((int) old.get("id") == (int) updated.get("id")) {
                    fileContent.set(i, updated.toString());
                    break;
                }
            }
            if (i == fileContent.size())
                return ReturnSate.NOT_EXISTS; // not exists element
            Files.write(Path.of(filepath), fileContent, StandardCharsets.UTF_8);
        } catch (JSONException | IOException c) {
            return ReturnSate.FAILURE;
        }
        return ReturnSate.SUCCESS;
    }

    @Override
    public ReturnSate remove(JSONObject jsonObject) {
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(Path.of(filepath), StandardCharsets.UTF_8));
            int i;
            for (i = 0; i < fileContent.size(); i++) {
                JSONObject old = new JSONObject(fileContent.get(i));
                if ((int) old.get("id") == (int) jsonObject.get("id")) {
                    break;
                }
            }
            if (i == fileContent.size())
                return ReturnSate.NOT_EXISTS; // not exists element
            fileContent.remove(i);
            Files.write(Path.of(filepath), fileContent, StandardCharsets.UTF_8);
        } catch (JSONException | IOException c) {
            return ReturnSate.FAILURE;
        }
        return ReturnSate.SUCCESS;
    }

    @Override
    public ArrayList<JSONObject> getAll() {
        List<String> fileContent;
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        try {
            fileContent = new ArrayList<>(Files.readAllLines(Path.of(filepath), StandardCharsets.UTF_8));
            for (String line : fileContent) {
                jsonObjects.add(new JSONObject(line));
            }
        } catch (IOException | JSONException e) {
            return null;
        }
        return jsonObjects;
    }


    // only for testing usage
    int getMaxId() {
        int id, max = 0;
        ArrayList<JSONObject> jsonObjects = getAll();
        for (JSONObject jsonObject : jsonObjects) {
            try {
                id = (int) jsonObject.get("id");
                if (id > max) max = id;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return max;
    }
}
