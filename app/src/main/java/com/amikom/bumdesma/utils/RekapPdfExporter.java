package com.amikom.bumdesma.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.text.TextUtils;

import com.amikom.bumdesma.model.RekapBulananItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RekapPdfExporter {

    private static final int PAGE_W = 595;
    private static final int PAGE_H = 842;
    private static final int MARGIN = 40;

    // Lebar kolom: No, Tanggal, Nama, Pokok, Bunga, Denda, Total (total = 515)
    private static final float[] COL_W = {20, 60, 130, 75, 65, 65, 100};

    private static final NumberFormat FMT_TABEL =
            NumberFormat.getInstance(new Locale("id", "ID"));
    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    static { FMT_TABEL.setMaximumFractionDigits(0); }

    public static File export(Context context, String periodeLabel,
                              double totalPokok, double totalBunga, double totalBayar,
                              List<RekapBulananItem> items) throws IOException {

        PdfDocument doc = new PdfDocument();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        PdfDocument.Page page = doc.startPage(
                new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, doc.getPages().size() + 1).create());
        Canvas canvas = page.getCanvas();

        float y = MARGIN;
        y = drawHeader(canvas, paint, periodeLabel, y);
        y = drawTableHeader(canvas, paint, y);

        int no = 1;
        for (RekapBulananItem item : items) {
            if (y + 18 > PAGE_H - MARGIN) {
                doc.finishPage(page);
                page = doc.startPage(new PdfDocument.PageInfo.Builder(
                        PAGE_W, PAGE_H, doc.getPages().size() + 1).create());
                canvas = page.getCanvas();
                y = MARGIN;
                y = drawTableHeader(canvas, paint, y);
            }
            y = drawRow(canvas, paint, no++, item, y);
        }

        y += 10;
        if (y + 60 > PAGE_H - MARGIN) {
            doc.finishPage(page);
            page = doc.startPage(new PdfDocument.PageInfo.Builder(
                    PAGE_W, PAGE_H, doc.getPages().size() + 1).create());
            canvas = page.getCanvas();
            y = MARGIN;
        }
        drawFooterTotal(canvas, paint, totalPokok, totalBunga, totalBayar, y);

        doc.finishPage(page);

        File dir = new File(context.getExternalFilesDir(null), "laporan");
        if (!dir.exists()) dir.mkdirs();
        String namaFile = "Rekap_Bulanan_" + periodeLabel.replace(" ", "_") + ".pdf";
        File file = new File(dir, namaFile);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            doc.writeTo(fos);
        } finally {
            doc.close();
        }
        return file;
    }

    private static float drawHeader(Canvas canvas, Paint paint, String periode, float y) {
        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("LAPORAN REKAP BULANAN", MARGIN, y, paint);
        y += 18;
        canvas.drawText("BUMDesma Randudongkal", MARGIN, y, paint);
        y += 20;

        paint.setFakeBoldText(false);
        paint.setTextSize(10f);
        canvas.drawText("Periode: " + periode, MARGIN, y, paint);
        y += 14;
        String dicetak = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id"))
                .format(new Date());
        canvas.drawText("Dicetak: " + dicetak, MARGIN, y, paint);
        y += 16;

        paint.setStrokeWidth(1f);
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint);
        return y + 14;
    }

    private static float drawTableHeader(Canvas canvas, Paint paint, float y) {
        paint.setTextSize(8f);
        paint.setFakeBoldText(true);
        String[] labels = {"No", "Tanggal", "Nama", "Pokok", "Bunga", "Denda", "Total"};
        float x = MARGIN;
        for (int i = 0; i < labels.length; i++) {
            canvas.drawText(labels[i], x, y, paint);
            x += COL_W[i];
        }
        y += 6;
        paint.setStrokeWidth(0.7f);
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint);
        paint.setFakeBoldText(false);
        return y + 12;
    }

    private static float drawRow(Canvas canvas, Paint paint, int no, RekapBulananItem item, float y) {
        paint.setTextSize(7.5f);
        float x = MARGIN;

        canvas.drawText(String.valueOf(no), x, y, paint); x += COL_W[0];
        canvas.drawText(nullToDash(item.getTanggalBayar()), x, y, paint); x += COL_W[1];

        String nama = nullToDash(item.getNamaLengkap());
        String namaClip = TextUtils.ellipsize(nama, new android.text.TextPaint(paint),
                COL_W[2] - 4, TextUtils.TruncateAt.END).toString();
        canvas.drawText(namaClip, x, y, paint); x += COL_W[2];

        canvas.drawText(FMT_TABEL.format(item.getJumlahPokok()), x, y, paint); x += COL_W[3];
        canvas.drawText(FMT_TABEL.format(item.getJumlahBunga()), x, y, paint); x += COL_W[4];
        canvas.drawText(FMT_TABEL.format(item.getJumlahDenda()), x, y, paint); x += COL_W[5];
        canvas.drawText(FMT_TABEL.format(item.getTotalBayar()), x, y, paint);

        return y + 16;
    }

    private static void drawFooterTotal(Canvas canvas, Paint paint,
                                        double pokok, double bunga, double bayar, float y) {
        paint.setStrokeWidth(0.7f);
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint);
        y += 16;

        paint.setTextSize(9f);
        paint.setFakeBoldText(true);
        canvas.drawText("Total Pokok: " + FMT_RUPIAH.format(pokok), MARGIN, y, paint);
        y += 14;
        canvas.drawText("Total Bunga: " + FMT_RUPIAH.format(bunga), MARGIN, y, paint);
        y += 14;
        canvas.drawText("Total Diterima: " + FMT_RUPIAH.format(bayar), MARGIN, y, paint);
    }

    private static String nullToDash(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }
}