package com.reStock.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Helper {
    static public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    static public class sortOrdersByDate implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            return o2.get_date().compareTo(o1.get_date());
        }
    }

    static public class sortOrdersByDistributor implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            return o1.get_distributor().compareTo(o2.get_distributor());
        }
    }

    static public class sortOrdersByProductName implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            return o1.get_product().compareTo(o2.get_product());
        }
    }

    static public class sortOrdersByStore implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            return o1.get_store_name().compareTo(o2.get_store_name());
        }
    }

    static public class sortOrdersByTotalCost implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            if (o1.get_total_cost() < o2.get_total_cost()) return -1;
            if (o1.get_total_cost() > o2.get_total_cost()) return 1;
            return 0;
        }
    }

    static public class sortProductsByName implements Comparator<Product> {
        @Override
        public int compare(Product o1, Product o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    static public class sortProductsByCost implements Comparator<Product> {
        @Override
        public int compare(Product o1, Product o2) {
            if (o1.getCost() < o2.getCost()) return -1;
            if (o1.getCost() > o2.getCost()) return 1;
            return 0;
        }
    }

    static public class sortStoresByName implements Comparator<Store> {
        @Override
        public int compare(Store o1, Store o2) {
            return o1.get_name().compareTo(o2.get_name());
        }
    }

    static public class sortStoresByEmail implements Comparator<Store> {
        @Override
        public int compare(Store o1, Store o2) {
            return o1.get_email().compareTo(o2.get_email());
        }
    }

    static public class sortDistributorByName implements Comparator<Distributor> {
        @Override
        public int compare(Distributor o1, Distributor o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    static public class sortDistributorByEmail implements Comparator<Distributor> {
        @Override
        public int compare(Distributor o1, Distributor o2) {
            return o1.getEmail().compareTo(o2.getEmail());
        }
    }

    public static String getRandomColor() {
        Random random = new Random();

        // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
        int nextInt = random.nextInt(0xffffff + 1);

        // format it as hexadecimal string (with hashtag and leading zeros)
        return String.format("#%06x", nextInt);
    }

    static public boolean saveExcelFile(Context context, Store store, List<Order> store_orders) {
        //sort list
        Collections.sort(store_orders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return o1.get_date().compareTo(o2.get_date());
            }
        });

        boolean success = false;

        //New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        //Cell style for header row
        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        //New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet(   store.get_name() + "_orders - " + getSimpleDate());

        // Generate column headings
        Row row = sheet1.createRow(0);

        c = row.createCell(0);
        c.setCellValue("Product Name");
        c.setCellStyle(cs);

        c = row.createCell(1);
        c.setCellValue("Order Date");
        c.setCellStyle(cs);

        c = row.createCell(2);
        c.setCellValue("Quantity");
        c.setCellStyle(cs);

        c = row.createCell(3);
        c.setCellValue("Total Cost");
        c.setCellStyle(cs);

        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));
        sheet1.setColumnWidth(3, (15 * 500));

        int prev_month = getMonth(store_orders.get(0).get_date());
        int curr_month;
        Row row1;
        int counter = 1;

        for (Order order: store_orders)
        {
            curr_month = getMonth(order.get_date());

            if (curr_month > prev_month){
                prev_month = curr_month;
                counter++;

                row1 = sheet1.createRow(counter);

                c = row1.createCell(0);
                c.setCellValue(order.get_product());

                c = row1.createCell(1);
                c.setCellValue(getSimpleDate(order.get_date()));

                c = row1.createCell(2);
                c.setCellValue(String.valueOf(order.get_quantity()));

                c = row1.createCell(3);
                c.setCellValue(String.valueOf(order.get_total_cost()));

                counter++;
            } else {
                row1 = sheet1.createRow(counter);

                c = row1.createCell(0);
                c.setCellValue(order.get_product());

                c = row1.createCell(1);
                c.setCellValue(getSimpleDate(order.get_date()));

                c = row1.createCell(2);
                c.setCellValue(String.valueOf(order.get_quantity()));

                c = row1.createCell(3);
                c.setCellValue(String.valueOf(order.get_total_cost()));

                counter++;
            }
        }

        // Create a path where we will place our List of objects on external storage
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + store_orders.get(0).get_distributor(), store.get_name() + ".xls");
        if (!file.exists()) {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + store_orders.get(0).get_distributor());
            directory.mkdirs();
        }
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    static public String getSimpleDate()
    {
        Date date = Calendar.getInstance().getTime();
        //
        // Display a date in day, month, year format
        //
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return formatter.format(date);
    }

    static public String getSimpleDate(Date date)
    {
        //
        // Display a date in day, month, year format
        //
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        return formatter.format(date);
    }

    static public Distributor document_to_distributor(DocumentSnapshot documentSnapshot){
        final Distributor distributor = new Distributor();

        distributor.setEmail(documentSnapshot.getString("Email"));
        distributor.setName(documentSnapshot.getString("Name"));

        return distributor;
    }
}
