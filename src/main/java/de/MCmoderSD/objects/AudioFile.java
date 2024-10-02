package de.MCmoderSD.objects;

import okhttp3.ResponseBody;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public class AudioFile {

    // Audio Data
    private final byte[] audioData;
    private final ByteArrayInputStream byteArrayInputStream;

    // Audio Components
    private AudioInputStream audioInputStream;
    private AudioFormat audioFormat;
    private DataLine.Info info;
    private SourceDataLine audioLine;

    // Byte array constructor
    public AudioFile(byte[] audioData) {

        // Set audio data
        this.audioData = audioData;
        byteArrayInputStream = new ByteArrayInputStream(audioData);

        // Initialize audio
        initializeAudio();
    }

    // Byte array constructor with audio format
    public AudioFile(byte[] audioData, AudioFormat format) {

        // Set audio data
        this.audioData = audioData;
        this.audioFormat = format;
        byteArrayInputStream = new ByteArrayInputStream(audioData);

        // Initialize audio
        audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioData.length / audioFormat.getFrameSize());
        initializeAudio();
    }

    // ResponseBody constructor
    public AudioFile(ResponseBody responseBody) {
        try {

            // Get audio data
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.writeBytes(responseBody.bytes());

            // Set audio data
            audioData = byteArrayOutputStream.toByteArray();
            byteArrayInputStream = new ByteArrayInputStream(audioData);

            // Initialize audio
            initializeAudio();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Initialize audio components
    private void initializeAudio() {
        try {

            // Check if audio format is null
            if (audioFormat == null) {
                audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);
                audioFormat = audioInputStream.getFormat();
            }

            // Check if the audio format is supported
            info = new DataLine.Info(SourceDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(info)) throw new UnsupportedAudioFileException("Audio format not supported!");

            // Open audio line
            audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(audioFormat);

        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            System.err.println("Error initializing audio: " + e.getMessage());
            audioLine = null;
            audioInputStream = null;
            audioFormat = null;
            info = null;
        }
    }

    // Play audio
    public void play() {
        if (audioLine == null) return;
        new Thread(() -> {
            try {
                // Start audio line
                audioLine.start();

                // Play audio
                byte[] allData = audioInputStream.readAllBytes();
                audioLine.write(allData, 0, allData.length);

                // Reset audio stream
                audioInputStream.reset();

                // Stop audio
                audioLine.drain();
                audioLine.stop();

            } catch (IOException e) {
                System.err.println("Error playing audio: " + e.getMessage());
            }
        }).start();
    }

    public void pause() {
        if (audioLine != null) audioLine.stop();
    }

    public void resume() {
        if (audioLine != null) audioLine.start();
    }

    public void close() {
        if (audioLine != null) audioLine.close();
    }

    public void reset() {
        try {
            if (audioInputStream != null) audioInputStream.reset();
        } catch (IOException e) {
            System.err.println("Error resetting audio: " + e.getMessage());
        }
    }

    // Getter
    public byte[] getAudioData() {
        return audioData;
    }

    public ByteArrayInputStream getByteArrayInputStream() {
        return byteArrayInputStream;
    }

    public AudioInputStream getAudioInputStream() {
        return audioInputStream;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public DataLine.Info getInfo() {
        return info;
    }

    public SourceDataLine getAudioLine() {
        return audioLine;
    }

    public int getSize() {
        return audioData.length;
    }

    public int getDuration() {
        return (int) ((float) audioData.length / audioFormat.getFrameSize() / audioFormat.getFrameRate());
    }

    public File exportToWav(String filePath) {
        try {
            File wavFile = new File(filePath);
            ByteArrayInputStream exportStream = new ByteArrayInputStream(audioData);
            AudioInputStream exportAudioStream = new AudioInputStream(exportStream, audioFormat, audioData.length / audioFormat.getFrameSize());
            AudioSystem.write(exportAudioStream, AudioFileFormat.Type.WAVE, wavFile);
            exportAudioStream.close();
            return wavFile;
        } catch (IOException e) {
            System.err.println("Error exporting audio to WAV: " + e.getMessage());
            return null;
        }
    }
}