package us.jacobdixon.discord.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SimpleFile {
    private String filename;
    private byte[] data;

    public SimpleFile(String filename, byte[] data){
        this.filename = filename;
        this.data = data;
    }

    public void writeTo(File parentFile) throws IOException {
        File outputFile = new File(parentFile.getAbsolutePath() + "/" + filename);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        fileOutputStream.write(data);
        fileOutputStream.close();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
