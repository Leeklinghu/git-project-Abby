import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;

public class Git{ //rename Only Filhasher to Git
    public static void main(String[] args) throws IOException {
        System.err.println(init(args));
        for (int i = 0; i < args.length; i++) {
            createBlob(args[i]);
        }
    }

    public static void createBlob(String filePath) throws IOException {
        String sha1hash = hashFile(filePath);
        // String content = new String(Files.readAllBytes(Paths.get(filePath)));
        File backup = new File("git/objects/ " + sha1hash);
    
        // FileWriter object = new FileWriter(("git/objects/" + sha1hash));
        // object.write(content);
        // object.close();
         File blobFile = new File("git/objects", sha1hash); //now it read by byte under the while loop

        if (!blobFile.exists()) {
            FileReader reader = new FileReader(filePath);
            FileWriter writer = new FileWriter(blobFile);

            int currentByte = reader.read();

            while (currentByte != -1) {
                writer.write(currentByte);
                currentByte = reader.read();
            }

            reader.close();
            writer.close();
        }

        System.out.println(backup);
        updateIndexFile(filePath);
    }

    public static String hashFile(String filePath) throws IOException {
        // TODO (FH-4): read the whole file, digest it, convert the bytes to hex
        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path))
            throw new IOException("No such files: " + filePath);

        byte[] fileBytes = Files.readAllBytes(path);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-1 is not avaliable", e);// SHA-256 instead of SHA-1
        }

        byte[] hash = digest.digest(fileBytes);

        return HexFormat.of().formatHex(hash);
    }

    public static void updateIndexFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        String fullPath = path.toAbsolutePath().toString();
        String rootPath = Path.of("").toAbsolutePath().toString();
        fullPath = path.toAbsolutePath().normalize().toString();
        if (fullPath.startsWith(rootPath + File.separator)) {
            fullPath = fullPath.substring(rootPath.length() + 1);
        }
        fullPath = fullPath.replace("\\", "/");
        String hash = hashFile(filePath);//changed sha1hash to hash
        String output = hash + " " + fullPath;
        Path indexPath = Path.of("git/index");
        if(Files.exists(indexPath)){
            ArrayList<String> lines = new ArrayList<>(Files.readAllLines(indexPath));
            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.equals(output)) {
                    return;
                }
                String linePath = line.substring(line.indexOf(" ") + 1);
                if (linePath.equals(fullPath)) {
                    lines.set(i, output);
                    changed = true;
                }
            }
            if (changed) {
                FileWriter rewriteIndex = new FileWriter("git/index");
                for (int i = 0; i < lines.size(); i++) {
                    rewriteIndex.write(lines.get(i) + System.lineSeparator());
                }
                rewriteIndex.close();
                return;
            }

        }
        FileWriter updatingIndex = new FileWriter(("git/index"), true);
        // String content = new String(Files.readAllBytes(Paths.get(filePath)));
        updatingIndex.write(hash + " " + fullPath+System.lineSeparator());
        updatingIndex.close();
        File backup = new File("git/index/" + hash + ".txt");
        System.out.println(backup);
        System.out.println(updatingIndex);
    }

    public static String createTree(ArrayList<String> workingList, String dirPath) throws IOException {
        String treeContent = "";
        for (int i = 0; i < workingList.size(); i++) {
            String[] parts = workingList.get(i).split(" ");
            String type = parts[0];
            String hash = parts[1];
            String entryPath = parts[2];
            int lastSlash = entryPath.lastIndexOf("/");
            String entryDir = entryPath.substring(0, lastSlash);
            String name = entryPath.substring(lastSlash + 1);
            if (entryDir.equals(dirPath)) {
                treeContent = treeContent + type + " " + hash + " " + name + "\n";
            }
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-1 is not avaliable", e);
        }
        String treeHash = HexFormat.of().formatHex(digest.digest(treeContent.getBytes()));

        File treeFile = new File("git/objects", treeHash);
        if (!treeFile.exists()) {
            FileWriter writer = new FileWriter(treeFile);
            writer.write(treeContent);
            writer.close();
        }
        return treeHash;
    }

    public static String init(String[] args) { //change output to a string that you return
        try {
            // TODO (FH-1): create the JavaFileSystem directory
            boolean folderMade =false;
            File root = new File("git/");
            if(!root.exists()){ //adding check statement 
                root.mkdirs();
                folderMade=true;
            }

            // make objects/ inside git/
            File backup = new File("git/objects/");
            if(!backup.exists()){
                 backup.mkdirs();
                 folderMade=true;
            }
            File indexFile = new File("git/index");
            if (!indexFile.exists()) {
                indexFile.createNewFile();
                folderMade=true;
            }
            File HeadFile = new File("git/HEAD");
            if (!HeadFile.exists()) {
                HeadFile.createNewFile();
                folderMade=true;
            }

            if (folderMade) {
                return "Git Repository Created";
            } else {
                return "Git Repository Already Exist"; 
            }
           

            // Create a file named index inside git/ if it does not already exist, with no
            // extension
        

            // Create a file named HEAD inside git/ if it does not already exist, with no
            // extension
          

            // print message
   

        } catch (IOException e) {
            return ("File error: " + e.getMessage());
        }
    }
}
