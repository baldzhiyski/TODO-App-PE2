package com.pe2.api.restController;

import com.pe2.api.domain.entity.ToDo;
import com.pe2.api.service.ToDosService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/csv-downloads")
public class ExportCsvController {

    private ToDosService todoService;

    public ExportCsvController(ToDosService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/todos")
    public ResponseEntity<String> downloadTodosCSV() throws IOException {
        List<ToDo> todos = todoService.getAllTodos();  // Assuming this fetches all ToDos from your DB

        StringWriter writer = new StringWriter();
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("id", "title", "description", "finished", "assignees", "createdDate", "dueDate", "finishedDate", "category"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (ToDo todo : todos) {
            csvPrinter.printRecord(
                    todo.getId(),
                    todo.getTitle(),
                    todo.getDescription(),
                    todo.isFinished(),
                    // Concatenate prename and name for each assignee
                    todo.getAssigneeList().stream()
                            .map(assignee -> assignee.getPrename() + " " + assignee.getName())  // Combine prename and name
                            .collect(Collectors.joining("+")),  // Collect the formatted assignees
                    // Format the dates if they are not null using SimpleDateFormat
                    formatDate(todo.getCreatedDate(), sdf),
                    formatDate(todo.getDueDate(), sdf),
                    formatDate(todo.getFinishedDate(), sdf),
                    todo.getCategory()
            );
        }

        csvPrinter.flush();

        // Set the response headers to trigger a file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=todos.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(writer.toString());
    }

    private String formatDate(Date date, SimpleDateFormat sdf) {
        return date != null ? sdf.format(date) : "";
    }
}
