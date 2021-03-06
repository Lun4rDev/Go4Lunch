package com.hernandez.mickael.go4lunch.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class BitmapDataObject implements Serializable {
    private String title;
    private int sourceWidth, currentWidth;
    private int sourceHeight, currentHeight;
    private Bitmap sourceImage;
    private Canvas sourceCanvas;
    private Bitmap currentImage;
    private Canvas currentCanvas;
    private Paint currentPaint;
    private Paint thumbnailPaint;
    private static final long serialVersionUID = 111696345129311948L;
    public byte[] imageByteArray;


    /**
     * Included for serialization - write this layer to the output stream.
     */
    public void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(title);
        out.writeInt(currentWidth);
        out.writeInt(currentHeight);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currentImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        BitmapDataObject bitmapDataObject = new BitmapDataObject();
        bitmapDataObject.imageByteArray = stream.toByteArray();

        out.writeObject(bitmapDataObject);
    }

    /**
     * Included for serialization - read this object from the supplied input stream.
     */
    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        title = (String) in.readObject();
        sourceWidth = currentWidth = in.readInt();
        sourceHeight = currentHeight = in.readInt();

        BitmapDataObject bitmapDataObject = (BitmapDataObject) in.readObject();
        Bitmap image = BitmapFactory.decodeByteArray(bitmapDataObject.imageByteArray, 0, bitmapDataObject.imageByteArray.length);

        sourceImage = Bitmap.createBitmap(sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
        currentImage = Bitmap.createBitmap(sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);

        sourceCanvas = new Canvas(sourceImage);
        currentCanvas = new Canvas(currentImage);

        currentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbnailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbnailPaint.setARGB(255, 200, 200, 200);
        thumbnailPaint.setStyle(Paint.Style.FILL);
    }
}