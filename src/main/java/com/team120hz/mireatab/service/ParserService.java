package com.team120hz.mireatab.service;

import com.team120hz.mireatab.model.Lesson;
import com.team120hz.mireatab.tools.Campus;
import com.team120hz.mireatab.tools.LessonType;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class ParserService {

    private static final HashMap<String, Campus> campuses = new HashMap<String, Campus>() {
        {
            put("В-78", Campus.V_78);
            put("В-86", Campus.V_86);
            put("МП-1", Campus.MP_1);
            put("С-20", Campus.S_20);
            put("СГ-22", Campus.SG_22);
        }
    };

    private static final HashMap<String, LessonType> lessonTypes = new HashMap<String, LessonType>() {
        {
            put("ЛК", LessonType.Lecture);
            put("ПР", LessonType.Seminar);
            put("ЛАБ", LessonType.Lab);
            put("СР", LessonType.Proj);
        }
    };

    /**
     * Загружает файл с расписанием
     *
     * @return Путь до загруженного файла с расписанием
     * @throws IOException Для загрузки файла
     */
    public static String fetch() throws IOException {
        BufferedInputStream in = new BufferedInputStream(new URL(
                "https://webservices.mirea.ru/upload/iblock/4b3/x949o5g2lanyzdn1n07krvd0c2ni4mho/III_1-kurs_22_23_vesna_10.04.2023.xlsx"
        ).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream("test.xlsx");
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }

        return "./test.xlsx";
    }

    /**
     * Получает номер строки, содержащей названия групп
     *
     * @param sheet Лист с расписанием
     * @return номер строки, содержащей названия групп
     */
    private static int findGroupNameRow(XSSFSheet sheet) {

        int rowNumber = 1;
        Iterator<Row> rowIter = sheet.rowIterator();
        int counter = 0;
        while (rowIter.hasNext()) {
            if (counter > 200) {
                break;
            }

            XSSFRow row = (XSSFRow) rowIter.next();

            Iterator<Cell> cellIter = row.cellIterator();

            while (cellIter.hasNext()) {
                Cell cell = cellIter.next();

                Pattern pattern = Pattern.compile("([А-Яа-я]{4}-[0-9]{2}-[0-9]{2})");
                if (pattern.matcher(cell.getStringCellValue()).find()) {
                    return counter;
                }
            }
            counter++;
        }

        return rowNumber;
    }

    private static void getLessonLocation() {

    }

    /**
     * Получение информации о неделях для пары из ее строки с названием
     *
     * @param lessonString строка с названием пары (может включать информацию о неделях проведения)
     * @param lesson       объект типа Lesson
     */
    private static void getLessonWeeks(String lessonString, Lesson lesson) {
        if (lessonString.contains("н.")) {
            String weekInfo = lessonString.split("н\\.")[0];
            weekInfo = weekInfo.replace(" ", "");
            boolean excluded = weekInfo.contains("кр.");
            if (excluded) {
                weekInfo = weekInfo.replace("кр.", "");
            }
            String[] weeks = weekInfo.split(",");
            for (String weekNumber : weeks) {
                if (weekNumber.contains("-")) {
                    String[] split = weekNumber.split("-");
                    int start = Integer.parseInt(split[0]);
                    int end = Integer.parseInt(split[1]);
                    for (int i = start; i < end + 1; i++) {
                        if (excluded) {
                            lesson.excludedWeeks.add(i);
                            continue;
                        }
                        lesson.includedWeeks.add(i);
                    }
                    continue;
                }
                if (excluded) {
                    lesson.excludedWeeks.add(Integer.parseInt(weekNumber));
                    continue;
                }
                lesson.includedWeeks.add(Integer.parseInt(weekNumber));
            }
        }
    }

    /**
     * Получает список пар конкретной группы
     *
     * @param sheet        лист с расписанием
     * @param groupNameRow номер строки с названиями групп
     * @param groupCol     номер столбца группы
     * @return список пар конкретной группы
     */
    private static ArrayList<Lesson> getGroupLessons(XSSFSheet sheet, int groupNameRow, int groupCol) {
        String groupName = sheet.getRow(groupNameRow).getCell(groupCol).getStringCellValue();

        int scheduleStartRow = groupNameRow + 2;
        ArrayList<Lesson> lessons = new ArrayList<>();
        for (int dayNumber = 0; dayNumber < 6; dayNumber++) {
            for (int pairNumber = 0; pairNumber < 7; pairNumber++) {
                for (int week = 0; week < 2; week++) {

                    String lessonName = sheet
                            .getRow(scheduleStartRow + dayNumber * 14 + pairNumber * 2 + week)
                            .getCell(groupCol).getStringCellValue().split("\n")[0];
                    Lesson lesson = new Lesson();
                    lesson.day = dayNumber + 1;
                    lesson.number = pairNumber + 1;
                    lesson.name = lessonName.contains("н.") ?
                            (lessonName.split("н\\.")[1]).substring(1) : lessonName;
                    lesson.evenWeek = week != 0;
                    if (Objects.equals(lessonName, "")) {
                        continue;
                    }
                    getLessonWeeks(lessonName, lesson);
                    lessons.add(lesson);
                }
            }
        }
        return lessons;
    }

    private static void test() throws IOException {
        String file = fetch();
        FileInputStream fs = new FileInputStream(file);
        XSSFWorkbook wb = new XSSFWorkbook(fs);
        XSSFSheet ws = wb.getSheetAt(0);
        int groupNameRowNumber = findGroupNameRow(ws);

        for (Lesson lesson : getGroupLessons(ws, groupNameRowNumber, 5)) {
            System.out.println();
            System.out.print(lesson.name + " ");
            System.out.print(lesson.day + " ");
            System.out.print(lesson.number + " ");
            System.out.print(lesson.excludedWeeks + " ");
            System.out.print(lesson.includedWeeks + " ");
            System.out.print(lesson.evenWeek + " ");
        }
    }
}
