package com.example.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.font.PdfEncodings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaterMarkEventHandler implements IEventHandler {

    private static final Logger LOGGER = Logger.getLogger(WaterMarkEventHandler.class.getName());
    private String waterMarkContent;
    private int waterMarkX;
    private int waterMarkY;
    private float rotationAngle;

    public WaterMarkEventHandler(String waterMarkContent) {
        this(waterMarkContent, 5, 5, (float) Math.toRadians(45)); // Default angle 45 degrees
    }

    public WaterMarkEventHandler(String waterMarkContent, int waterMarkX, int waterMarkY) {
        this(waterMarkContent, waterMarkX, waterMarkY, (float) Math.toRadians(45)); // Default angle 45 degrees
    }
    
    public WaterMarkEventHandler(String waterMarkContent, int waterMarkX, int waterMarkY, float rotationAngleInRadians) {
        this.waterMarkContent = waterMarkContent;
        this.waterMarkX = waterMarkX;
        this.waterMarkY = waterMarkY;
        this.rotationAngle = rotationAngleInRadians;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
        PdfDocument document = documentEvent.getDocument();
        PdfPage page = documentEvent.getPage();
        Rectangle pageSize = page.getPageSize();
        int pageNumber = document.getPageNumber(page);

        PdfFont pdfFont = null;
        try {
            pdfFont = PdfFontFactory.createFont("STSongStd-Light", PdfEncodings.IDENTITY_H);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "STSongStd-Light font for watermark not found, using default font.", e);
            try {
                 pdfFont = PdfFontFactory.createFont(); 
            } catch (IOException ex) {
                 LOGGER.log(Level.SEVERE, "Default font for watermark could not be created.", ex);
                return; 
            }
        }
        
        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), document);

        Paragraph waterMarkParagraph = new Paragraph(waterMarkContent).setOpacity(0.5f);
        if (pdfFont != null) {
            waterMarkParagraph.setFont(pdfFont);
        }
        // Use try-with-resources for Canvas
        try (Canvas canvas = new Canvas(pdfCanvas, pageSize)) {
            canvas.setFontColor(ColorConstants.LIGHT_GRAY)
                  .setFontSize(16);
                  // Font is set on paragraph directly

            for (int i = 0; i < waterMarkX; i++) {
                for (int j = 0; j < waterMarkY; j++) {
                    float xPos = (pageSize.getWidth() / (waterMarkX + 1)) * (i + 1);
                    float yPos = (pageSize.getHeight() / (waterMarkY + 1)) * (j + 1);
                    canvas.showTextAligned(waterMarkParagraph, xPos, yPos, pageNumber, TextAlignment.CENTER, VerticalAlignment.MIDDLE, rotationAngle);
                }
            }
        } 
    }
} 