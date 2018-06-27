/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.kiss.seeder;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author KICSI
 */
public class SeederAdmin {

    private static final String password = "test";
    private static final String user = "root";
    private static final String host = "192.168.0.19";

    private Session session = null;

    public SeederAdmin() {
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            
            System.out.println("Connected");
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    /**
     * JSch Example Tutorial Java SSH Connection Program
     */
    public static void main(String[] args) {
        SeederAdmin sa = new SeederAdmin();
//        sa.umount();
        sa.mount();
    }

    public String runCommand(String command) {

        StringBuilder response = new StringBuilder("");
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] tmp = new byte[1024];

            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }

                    response.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            System.out.println("DONE");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        System.out.println("Response: "+response.toString());
        return response.toString();
    }

    public void umount(){
        connect();
        pause();
        runCommand("umount \\-a");
        disconnect();
    }
    
    public void mount(){
        String resp = runCommand("fdisk -l");
        String[] lines = resp.split("\\n");
        String device = lines[lines.length-1].split(" ")[0];
        
        runCommand("mount -t auto --target /mnt --source "+device);
        
        connect();
        resume();
        
        disconnect();
    }
    
    public void connect() {
        runCommand("deluge-console connect");

    }

    public void pause() {
        runCommand("deluge-console pause \\*");
    }

    public void resume() {
        runCommand("deluge-console resume \\*");
    }

    public void disconnect() {
        session.disconnect();
        
    }

}
