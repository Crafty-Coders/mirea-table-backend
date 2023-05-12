package com.team120hz.mireatab.service;

import com.team120hz.mireatab.model.Group;
import com.team120hz.mireatab.model.Lesson;
import com.team120hz.mireatab.tools.Campus;
import com.team120hz.mireatab.tools.LessonType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ParserService {

    private final GroupService groupService;
    private final LessonService lessonService;

    @Autowired
    public ParserService(GroupService groupService, LessonService lessonService) {
        this.groupService = groupService;
        this.lessonService = lessonService;
    }

    /**
     * Форматирует строку с названием группы
     *
     * @param unformatted неформатированная строка
     * @return форматированная строка
     */
    private String formatGroupName(String unformatted) {
        return unformatted
                .replaceAll("\\s{2,}", " ")
                .replaceAll("\\s*-\\s*", "-")
                .split(" ")[0];
    }

    private final HashMap<String, Campus> campuses = new HashMap<>() {
        {
            put("В-78", Campus.V_78);
            put("В-86", Campus.V_86);
            put("МП-1", Campus.MP_1);
            put("С-20", Campus.S_20);
            put("СГ-22", Campus.SG_22);
        }
    };

    private final HashMap<String, LessonType> lessonTypes = new HashMap<>() {
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
    public String downloadFileFromUrl(String url, int number) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        Files.createDirectories(Paths.get("./data"));
        String name = "./data/" + number + ".xlsx";
        FileOutputStream fileOutputStream = new FileOutputStream(name);
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }

        return name;
    }

    /**
     * Получает номер строки, содержащей названия групп
     *
     * @param sheet Лист с расписанием
     * @return номер строки, содержащей названия групп
     */
    private int findGroupNameRow(XSSFSheet sheet) {

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
                try {
                    if (pattern.matcher(cell.getStringCellValue()).find()) {
                        return counter;
                    }
                } catch (IllegalStateException ignored) {
                }
            }
            counter++;
        }

        return rowNumber;
    }

    /**
     * Получение информации о о аудитории и кампусе проведения пары
     *
     * @param locationString строка с данными о аудитории и кампусе проведения
     * @param lesson         объект типа Lesson
     */
    private void getLessonLocation(String locationString, Lesson lesson) {
        String[] split = locationString.split("\n")[0].split("\\(");
        String auditory = split[0];
        if (split.length == 1) {
            lesson.auditory = locationString;
            return;
        }
        String campus = split[1].replace(")", "");
        lesson.campus = campuses.get(campus);
        lesson.auditory = auditory.substring(0, auditory.length() - 1);
    }

    private void getLessonTeachers(String teachersString, Lesson lesson) {
        lesson.teachers.addAll(Arrays.asList(teachersString.split("\n")));
    }

    /**
     * Получение информации о неделях для пары из ее строки с названием
     *
     * @param lessonString строка с названием пары (может включать информацию о неделях проведения)
     * @param lesson       объект типа Lesson
     */
    private void getLessonWeeks(String lessonString, Lesson lesson) {
        if (lessonString.contains("н.")) {
            String weekInfo = lessonString.split("н\\.")[0];
            weekInfo = weekInfo.replace(" ", "");
            boolean excluded = weekInfo.contains("кр");
            if (excluded) {
                weekInfo = weekInfo
                        .replace("кр.", "")
                        .replace("кр", "");

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
     * Получает пары конкретной группы и добавляет их в базу данных
     *
     * @param sheet        лист с расписанием
     * @param groupNameRow номер строки с названиями групп
     * @param groupCol     номер столбца группы
     */
    private void getGroupLessons(XSSFSheet sheet, int groupNameRow, int groupCol) {
        String groupName = formatGroupName(
                sheet.getRow(groupNameRow)
                        .getCell(groupCol)
                        .getStringCellValue()
        );
        Group group = new Group();
        group.name = groupName;

        List<Group> groups = groupService.findAllByName(groupName);

        if (groups.size() == 0) {
            group = groupService.save(group);
        } else {
            group = groups.get(0);
        }

        int scheduleStartRow = groupNameRow + 2;

        for (int dayNumber = 0; dayNumber < 6; dayNumber++) {
            for (int pairNumber = 0; pairNumber < 7; pairNumber++) {
                for (int week = 0; week < 2; week++) {
                    int lessonRow = scheduleStartRow + dayNumber * 14 + pairNumber * 2 + week;

                    XSSFCell lessonCell = sheet
                            .getRow(lessonRow)
                            .getCell(groupCol);

                    if (lessonCell == null) {
                        continue;
                    }

                    String lessonName = lessonCell
                            .getStringCellValue()
                            .split("\n")[0];

                    if (Objects.equals(lessonName, "")) {
                        continue;
                    }

                    Lesson lesson = new Lesson();
                    lesson.day = dayNumber + 1;
                    lesson.number = pairNumber + 1;
                    lesson.name = lessonName.contains("н.") ?
                            (lessonName.split("н\\.")[1]).substring(1)
                            : lessonName;
                    lesson.evenWeek = week != 0;
                    lesson.type = lessonTypes.get(sheet
                            .getRow(lessonRow)
                            .getCell(groupCol + 1)
                            .getStringCellValue().split("\n")[0]);

                    XSSFCell locationCell = sheet
                            .getRow(lessonRow)
                            .getCell(groupCol + 3);

                    String locationString = locationCell.getCellType() == Cell.CELL_TYPE_NUMERIC
                            ? Double.toString(locationCell.getNumericCellValue())
                            : locationCell.getStringCellValue();

                    String teachersString = sheet
                            .getRow(lessonRow)
                            .getCell(groupCol + 2)
                            .getStringCellValue();
                    getLessonWeeks(lessonName, lesson);
                    getLessonLocation(locationString, lesson);
                    getLessonTeachers(teachersString, lesson);

                    lesson.group = group;

                    lessonService.save(lesson); // add to DB
                }
            }
        }
    }

    /**
     * Получает расписание из файла
     *
     * @param file файл, который нужно распарсить
     * @throws IOException
     */
    private void parseFile(String file) throws IOException {
        FileInputStream fs = new FileInputStream(file);
        XSSFWorkbook wb = new XSSFWorkbook(fs);
        XSSFSheet ws = wb.getSheetAt(0);
        int groupNameRowNumber = findGroupNameRow(ws);

        boolean big = false;
        int i = 5;

        XSSFCell cell = ws.getRow(groupNameRowNumber).getCell(i);
        while (cell != null && !Objects.equals(cell.getStringCellValue(), "")) {
            getGroupLessons(ws, groupNameRowNumber, i);
            i += big ? 10 : 5;
            big = !big;
            cell = ws.getRow(groupNameRowNumber).getCell(i);
        }
    }

    private void downloadScheduleToDatabase(ArrayList<String> urls) throws IOException {

        for (int i = 0; i < urls.size(); i++) {
            String file = downloadFileFromUrl(urls.get(i), i);
            parseFile(file);
            (new File(file)).delete();
        }
    }

    /**
     * Получает массив ссылок на файлы со страницы с расписанием
     *
     * @return массив ссылок на файлы с расписанием
     * @throws IOException
     */
    private ArrayList<String> getUrls() throws IOException {
        String scheduleUrl = "https://www.mirea.ru/schedule/";
        Document document = Jsoup
                .connect(scheduleUrl)
                .userAgent("Mozilla")
                .get();

        Elements a = document.select("a");
        ArrayList<String> urls = new ArrayList<>();

        a.forEach(el -> {
            if (el.attr("href").contains("xlsx")
                    && !el.attr("href").contains("ekz")
                    && !el.attr("href").contains("sessiya")
            ) {
                urls.add(el.attr("href"));
            }
        });

        return urls;

    }

    public void parse() throws IOException {
        ArrayList<String> urls = getUrls();
        downloadScheduleToDatabase(urls);
    }
}
