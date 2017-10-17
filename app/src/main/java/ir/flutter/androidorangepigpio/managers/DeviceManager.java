package ir.flutter.androidorangepigpio.managers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ir.flutter.androidorangepigpio.BuildConfig;
import ir.flutter.androidorangepigpio.enums.EnPinDirection;
/**
 * Created by m.beiranvand on 2017-10-17.
 */

public class DeviceManager {

    /**
     * run sudo command and take result
     * @param strings of commands
     * @return result of sudo command
     */
    private static String sudoCmd(String... strings) {
        String res = "";
        Process su = null;
        DataOutputStream outputStream = null;
        InputStream response = null;
        try{
            su = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

        } catch (IOException ignored){

        }
        if(outputStream!=null && su!=null)
        {
            try{
                for (String s : strings) {
                    outputStream.writeBytes(s+"\n");
                    outputStream.flush();
                }
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                su.waitFor();
                res = readFully(response);

            }catch (Exception ignored){}
        }
        try {
            if(outputStream != null)
                outputStream.close();
            if(response != null)
                response.close();
        } catch (IOException ignored) {
        }

        return res;
    }

    /**
     * with this method you can get current cpu temperature
     * @return cpu them
     */
    public static int getCpuTemp() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
            p = Runtime.getRuntime().exec("cat /sys/class/thermal/thermal_zone0/temp");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = reader.readLine();
            if(BuildConfig.DEBUG)
                Log.d("getCpuTemp" , line);
            return Integer.parseInt(line) ;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * check current pin status
     * @param PIN pin number from GPIO map
     * @return true : is already on(High), and false : off(Low) , null : unknown state
     */
    @Nullable
    public static Boolean getPinStatus(@NonNull String PIN) {
        Process p;
        try {

            p = Runtime.getRuntime().exec("cat /sys/class/gpio/gpio"+PIN+"/value");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = reader.readLine();
            reader.close();
            if(line == null)
                return null;
            return "1".equals(line.trim()) ;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * set Pin Direction
     * @param PIN pin number from GPIO map
     * @param Direction in : for input , out : for output
     */
    public static void setPinDir(@NonNull String PIN,@NonNull EnPinDirection Direction){
        String[] command = new String[]{
                "echo "+PIN+" > /sys/class/gpio/export",
                "echo \""+Direction.getDir()+"\" > /sys/class/gpio/gpio"+PIN+"/direction",
        };

        sudoCmd(command);
    }

    /**
     * set pin to high level or 5V
     * @param PIN pin number from GPIO map
     */
    private static void setPinOn(@NonNull String PIN)
    {
        String[] command = new String[]{
                "echo 1 > /sys/class/gpio/gpio"+PIN+"/value"
        };

        sudoCmd(command);
    }

    /**
     * set pin to low level or 0V
     * @param PIN pin number from GPIO map
     */
    private static void setPinOff(@NonNull String PIN)
    {
        String[] command = new String[]{
                "echo 0 > /sys/class/gpio/gpio"+PIN+"/value"
        };

        sudoCmd(command);
    }

    /**
     * read input stream and return as a string
     * @param is input stream
     * @return result of stream
     * @throws IOException occurred if can't read stream from io
     */
    private static String readFully(@NonNull InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }

}
