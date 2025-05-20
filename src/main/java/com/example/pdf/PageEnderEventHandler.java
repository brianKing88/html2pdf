package com.example.pdf;

import com.itextpdf.kernel.colors.DeviceRgb;
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
import com.itextpdf.io.font.PdfEncodings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PageEnderEventHandler implements IEventHandler {

    private static final Logger LOGGER = Logger.getLogger(PageEnderEventHandler.class.getName());
    private String pageEnderContent;

    public PageEnderEventHandler(String pageEnderContent) {
        this.pageEnderContent = pageEnderContent;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent pdfDocumentEvent = (PdfDocumentEvent) event;
        PdfPage page = pdfDocumentEvent.getPage();
        Rectangle pageSize = page.getPageSize();
        PdfDocument pdfDoc = pdfDocumentEvent.getDocument();

        // 不在第一页添加页脚 (原文逻辑是这样，如果需要在第一页也显示页脚，可以移除此判断)
        if (pdfDoc.getPageNumber(page) != 1) {
            PdfFont pdfFont = null;
            try {
                pdfFont = PdfFontFactory.createFont("STSongStd-Light", PdfEncodings.IDENTITY_H);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "STSongStd-Light font for page ender not found, using default font.", e);
                 try {
                     pdfFont = PdfFontFactory.createFont(); // Default font
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Default font for page ender could not be created.", ex);
                    return; // Cannot proceed without a font
                }
            }

            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            // Use try-with-resources for Canvas
            try (Canvas canvas = new Canvas(pdfCanvas, pageSize)) {
                float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
                float y = pageSize.getBottom() + 32; // Position from bottom
                Paragraph paragraph = new Paragraph(pageEnderContent)
                        .setFontSize(7)
                        .setFont(pdfFont);
                canvas.showTextAligned(paragraph, x, y, TextAlignment.CENTER);
            } // canvas is closed here

            // 在页面底部绘制分隔线
            pdfCanvas.setStrokeColor(new DeviceRgb(60, 60, 60))
                    .setLineWidth(0.5f)
                    .moveTo(pageSize.getLeft() + 80, pageSize.getBottom() + 27)
                    .lineTo(pageSize.getRight() - 80, pageSize.getBottom() + 27)
                    .stroke();
        }
    }
} 