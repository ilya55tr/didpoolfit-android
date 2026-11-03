package by.ilyatr.didpoolfit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Фотографии храним в files/avatars; наружу отдаём только временный файл камеры. */
final class ProfilePhotos {
    private ProfilePhotos() {}

    static File file(Context context, String name) {
        if (name == null || !name.matches("avatar_[0-9]+\\.jpg")) return null;
        return new File(new File(context.getFilesDir(), "avatars"), name);
    }

    static void show(Context context, ImageView view, String name, String gender) {
        File file = file(context, name);
        Bitmap bitmap = file != null && file.isFile() ? BitmapFactory.decodeFile(file.getPath()) : null;
        if (bitmap != null) view.setImageBitmap(bitmap);
        else view.setImageResource("Female".equals(gender)
                ? R.drawable.avatar_default_female : R.drawable.avatar_default_male);
    }

    static String importPhoto(Context context, Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, options);
        }
        if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("Invalid image");
        options.inSampleSize = 1;
        while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > 1024) options.inSampleSize *= 2;
        options.inJustDecodeBounds = false;
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, options);
        }
        if (bitmap == null) throw new IOException("Image cannot be decoded");
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            orientation = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException ignored) { /* Изображение может не содержать EXIF. */ }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1, -1); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(270); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(270); break;
            default: break;
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotated != bitmap) bitmap.recycle();
        File directory = new File(context.getFilesDir(), "avatars");
        if (!directory.isDirectory() && !directory.mkdirs()) {
            rotated.recycle();
            throw new IOException("Cannot create avatars directory");
        }
        File destination = File.createTempFile("avatar_", ".jpg", directory);
        try (FileOutputStream out = new FileOutputStream(destination)) {
            if (!rotated.compress(Bitmap.CompressFormat.JPEG, 90, out)) throw new IOException("Cannot encode image");
        } catch (IOException e) {
            destination.delete();
            throw e;
        } finally {
            rotated.recycle();
        }
        return destination.getName();
    }

    static void delete(Context context, String name) {
        File file = file(context, name);
        if (file != null && file.isFile()) file.delete();
    }
}
