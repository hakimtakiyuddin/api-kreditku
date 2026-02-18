package com.kreditku.api_kreditku.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelService {

    public Map<String, Double> parseExpenses(MultipartFile file) throws IOException {
        String filename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();

        if (filename.endsWith(".csv")) {
            return parseCsv(file);
        } else if (filename.endsWith(".xls")) {
            return parseExcel(file, false);
        } else {
            return parseExcel(file, true);
        }
    }

    private Map<String, Double> parseExcel(MultipartFile file, boolean isXlsx) throws IOException {
        Map<String, Double> expenses = new LinkedHashMap<>();

        try (Workbook workbook = isXlsx
                ? new XSSFWorkbook(file.getInputStream())
                : new HSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                Cell categoryCell = row.getCell(0);
                Cell amountCell = row.getCell(1);

                if (categoryCell == null || amountCell == null)
                    continue;

                String category = categoryCell.getStringCellValue().trim();
                double amount = amountCell.getNumericCellValue();

                if (!category.isEmpty() && amount > 0) {
                    expenses.merge(category, amount, (existing, newAmount) -> existing + newAmount); // ← changed
                }
            }

        }

        return expenses;
    }

    private Map<String, Double> parseCsv(MultipartFile file) throws IOException {
        Map<String, Double> expenses = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 2)
                    continue;

                String category = parts[0].trim();
                double amount = Double.parseDouble(parts[1].trim());

                if (!category.isEmpty() && amount > 0) {
                    expenses.merge(category, amount, (existing, newAmount) -> existing + newAmount); // ← changed
                }
            }

        }

        return expenses;
    }

    public String formatExpensesAsText(Map<String, Double> expenses) {
        StringBuilder sb = new StringBuilder();
        expenses.forEach((category, amount) -> sb.append("- ")
                .append(category)
                .append(": RM")
                .append(String.format("%.2f", amount))
                .append("\n"));
        return sb.toString();
    }
}
