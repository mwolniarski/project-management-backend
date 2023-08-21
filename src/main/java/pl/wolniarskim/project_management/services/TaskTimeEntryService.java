package pl.wolniarskim.project_management.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import pl.wolniarskim.project_management.mappers.TaskTimeEntryMapper;
import pl.wolniarskim.project_management.models.*;
import pl.wolniarskim.project_management.models.DTO.TaskTimeEntryReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskTimeEntryWriteModel;
import pl.wolniarskim.project_management.repositories.ProjectRepository;
import pl.wolniarskim.project_management.repositories.TaskRepository;
import pl.wolniarskim.project_management.repositories.TaskTimeEntryRepository;
import pl.wolniarskim.project_management.utils.SecurityUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;
import static pl.wolniarskim.project_management.utils.SecurityUtil.getLoggedUserId;
import static pl.wolniarskim.project_management.utils.SecurityUtil.isUserHavingPermission;

@Service
@AllArgsConstructor
public class TaskTimeEntryService {

    private final TaskTimeEntryRepository taskTimeEntryRepository;
    private final TaskRepository taskRepository;
    private final TaskTimeEntryMapper taskTimeEntryMapper;
    private final ProjectRepository projectRepository;

    public TaskTimeEntryReadModel addTimeEntry(TaskTimeEntryWriteModel taskTimeEntryWriteModel, long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow();

        SecurityUtil.checkIfUserIsPartOfOrganization(task.getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(TIME_ENTRY_ADD);

        TaskTimeEntry taskTimeEntry = taskTimeEntryMapper.toTaskTimeEntry(taskTimeEntryWriteModel);
        taskTimeEntry.setTask(task);
        taskTimeEntry.setUser(SecurityUtil.getLoggedUser());
        return taskTimeEntryMapper.fromTimeEntry(taskTimeEntryRepository.save(taskTimeEntry));
    }

    public void deleteTimeEntry(long taskTimeEntryId){
        TaskTimeEntry taskTimeEntry = taskTimeEntryRepository.findById(taskTimeEntryId).orElseThrow();
        SecurityUtil.checkIfUserIsPartOfOrganization(taskTimeEntry.getTask().getTaskGroup().getProject().getOrganization().getOrgId());
        SecurityUtil.checkUserPermission(TIME_ENTRY_REMOVE);
        taskTimeEntryRepository.deleteById(taskTimeEntryId);
    }

    public List<TaskTimeEntryReadModel> getTimeEntries(long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();
        SecurityUtil.checkIfUserIsPartOfOrganization(project.getOrganization().getOrgId());

        if(isUserHavingPermission(TIME_ENTRY_READ_ALL)){
            return taskTimeEntryRepository.findAllByProjectId(projectId).stream()
                    .map(taskTimeEntryMapper::fromTimeEntry)
                    .collect(Collectors.toList());
        }
        else{
            return taskTimeEntryRepository.findAllByUserIdAndProjectId(getLoggedUserId(), projectId).stream()
                    .map(taskTimeEntryMapper::fromTimeEntry)
                    .collect(Collectors.toList());
        }
    }

    public List<TaskTimeEntryReadModel> getTimeEntriesForTask(long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow();
        SecurityUtil.checkIfUserIsPartOfOrganization(task.getTaskGroup().getProject().getOrganization().getOrgId());

        return taskTimeEntryRepository.findAllByTask_Id(taskId).stream()
                .map(taskTimeEntryMapper::fromTimeEntry)
                .collect(Collectors.toList());
    }
    public void exportTimeEntry(HttpServletResponse response, long projectId) throws IOException {

        SecurityUtil.checkUserPermission(TIME_ENTRY_READ_ALL);

        response.setContentType("text/csv");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);

        List<TaskTimeEntryReadModel> timeEntries = getTimeEntries(projectId);

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"Hours spent", "Time description"};
        String[] nameMapping = {"hoursSpent", "description"};

        csvWriter.writeHeader(csvHeader);

        for (TaskTimeEntryReadModel timeEntry : timeEntries) {
            csvWriter.write(timeEntry, nameMapping);
        }

        csvWriter.close();
    }
}
