import java.io.*;
import java.util.*;

import com.adobe.xmp.*;
import com.adobe.xmp.impl.*;
import com.adobe.xmp.impl.Base64;

public class GetData
{
    // An encoding should really be specified here, and for other uses of getBytes!
    private static final byte[] OPEN_ARR = "<x:xmpmeta".getBytes();
    private static final byte[] CLOSE_ARR = "</x:xmpmeta>".getBytes();

    private static void copy(InputStream in, OutputStream out) throws IOException
    {
        int len = -1;
        byte[] buf = new byte[1024];
        while((len = in.read(buf)) >= 0)
        {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

    private static int indexOf(byte[] arr, byte[] sub, int start)
    {
        int subIdx = 0;

        for(int x = start;x < arr.length;x++)
        {
            if(arr[x] == sub[subIdx])
            {
                if(subIdx == sub.length - 1)
                {
                    return x - subIdx;
                }
                subIdx++;
            }
            else
            {
                subIdx = 0;
            }
        }

        return -1;
    }

    private static String fixString(String str)
    {
        int idx = 0;
        StringBuilder buf = new StringBuilder(str);
        while((idx = buf.indexOf("http")) >= 0)
        {
            buf.delete(idx - 4, idx + 75);
        }

        return buf.toString();
    }

    private static String findDepthData(File file) throws IOException, XMPException
    {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        copy(in, out);
        byte[] fileData = out.toByteArray();

        int openIdx = indexOf(fileData, OPEN_ARR, 0);
        while(openIdx >= 0)
        {
            int closeIdx = indexOf(fileData, CLOSE_ARR, openIdx + 1) + CLOSE_ARR.length;

            byte[] segArr = Arrays.copyOfRange(fileData, openIdx, closeIdx);
            XMPMeta meta = XMPMetaFactory.parseFromBuffer(segArr);

            String str = meta.getPropertyString("http://ns.google.com/photos/1.0/depthmap/", "Data");

            if(str != null)
            {
                return fixString(str);
            }

            openIdx = indexOf(fileData, OPEN_ARR, closeIdx + 1);
        }

        return null;
    }

    public static void main(String[] args) throws Exception
    {
        long startTime = System.nanoTime();
        String data = findDepthData(new File("IMG_20150116_143419.jpg"));
        System.out.println("Finding data took: " + ((System.nanoTime() - startTime)*1000000000) + " ns");
        if(data != null)
        {
            byte[] imgData = Base64.decode(data.getBytes());
            ByteArrayInputStream in = new ByteArrayInputStream(imgData);
            FileOutputStream out = new FileOutputStream(new File("out.png"));
            copy(in, out);
        }
    }
}