package utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import web.GSCUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ExcelUtils {

    public static void save(File file, List<GSCUser> users) throws IOException {
        if (users == null || users.size() == 0) {
            return;
        }
        try(HSSFWorkbook book = new HSSFWorkbook()){
            String sheetName = file.getName().substring(0, file.getName().contains(".")? file.getName().lastIndexOf("."): file.getName().length());
            Sheet sheet = book.createSheet(sheetName);
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Имя");
            row.createCell(1).setCellValue("Id");
            row.createCell(2).setCellValue("Цитованість");
            row.createCell(3).setCellValue("Індекс Гірша");
            row.createCell(4).setCellValue("Присутність");
            row.createCell(5).setCellValue("url");

            for (int i = 0; i < users.size(); i++) {
                row = sheet.createRow(i + 1);

                Cell name = row.createCell(0);
                name.setCellValue(users.get(i).getShortedName());

                Cell id = row.createCell(1);
                id.setCellValue(users.get(i).getId());

                Cell citation = row.createCell(2);
                citation.setCellValue(users.get(i).getCitationStatistics());

                Cell hIndex = row.createCell(3);
                hIndex.setCellValue(users.get(i).getHIndex());

                Cell presence = row.createCell(4);
                presence.setCellValue(users.get(i).getPresence());

                Cell userPageURL = row.createCell(5);
                userPageURL.setCellValue(users.get(i).getUserPageURL());
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }
            book.write(file);
        }
    }

    public static List<String> readIds(File file) throws IOException {
        try(HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = book.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row headRow = rowIterator.next();
            int indexOfId = 1;
            Iterator<Cell> headCells = headRow.cellIterator();
            while (headCells.hasNext()) {
                Cell cell = headCells.next();
                if (cell.getStringCellValue().toLowerCase().equals("id")) {
                    indexOfId = cell.getColumnIndex();
                }
            }

            List<String> indices = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                indices.add(row.getCell(indexOfId).getStringCellValue());
            }
            return indices;
        }
    }

    public static void update(File file, List<GSCUser> users) throws IOException {
        try(HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = book.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row headRow = rowIterator.next();
            int indexOfId = 1;
            Iterator<Cell> headCells = headRow.cellIterator();
            while (headCells.hasNext()) {
                Cell cell = headCells.next();
                if (cell.getStringCellValue().toLowerCase().equals("id")) {
                    indexOfId = cell.getColumnIndex();
                }
            }

            int usersIndex = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    row.getCell(indexOfId + 1).setCellValue(users.get(usersIndex).getCitationStatistics());
                } catch (NullPointerException e) {
                    row.createCell(indexOfId + 1).setCellValue(users.get(usersIndex).getCitationStatistics());
                }


                try {
                    row.getCell(indexOfId + 2).setCellValue(users.get(usersIndex).getHIndex());
                } catch (NullPointerException e) {
                    row.createCell(indexOfId + 2).setCellValue(users.get(usersIndex).getHIndex());
                }

                try {
                    row.getCell(indexOfId + 3).setCellValue(users.get(usersIndex).getPresence());
                } catch (NullPointerException e) {
                    row.createCell(indexOfId + 3).setCellValue(users.get(usersIndex).getPresence());
                }

                usersIndex++;
            }

            book.write(file);
        }
    }
}
