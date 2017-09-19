package com.weidi.imageload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImgUtils {

    /**
     * @param urlStr
     * @param file
     * @return true表示下载成功
     */
    public static boolean downloadImgByUrl(String urlStr, File file) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            is = conn.getInputStream();
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[512];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            conn.disconnect();
            conn = null;
            buffer = null;
            url = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * @param urlStr
     * @param imageview
     * @return 不为null表示下载成功
     */
    public static Bitmap downloadImgByUrl(String urlStr, ImageView imageview) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(conn.getInputStream());
            is.mark(is.available());
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            // Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
            Bitmap bitmap = null;
            // 获取imageview想要显示的宽和高
            ImageSizeUtils.ImageSize imageViewSize = ImageSizeUtils.getImageViewSize(imageview);
            opts.inSampleSize = ImageSizeUtils.caculateInSampleSize(
                    opts, imageViewSize.width, imageViewSize.height);
            opts.inJustDecodeBounds = false;
            is.reset();
            bitmap = BitmapFactory.decodeStream(is, null, opts);
            conn.disconnect();
            conn = null;
            url = null;
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
