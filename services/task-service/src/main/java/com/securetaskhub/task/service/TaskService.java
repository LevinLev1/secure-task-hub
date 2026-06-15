package com.securetaskhub.task.service;

import com.securetaskhub.common.observability.AuditTrailService;
import com.securetaskhub.task.dto.TaskRequest;
import com.securetaskhub.task.dto.TaskResponse;
import com.securetaskhub.task.model.Task;
import com.securetaskhub.task.model.TaskStatus;
import com.securetaskhub.task.repository.TaskRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AuditTrailService auditTrailService;

    public TaskService(TaskRepository taskRepository, AuditTrailService auditTrailService) {
        this.taskRepository = taskRepository;
        this.auditTrailService = auditTrailService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findVisibleTasks(Authentication authentication) {
        if (isAdmin(authentication)) {
            return taskRepository.findAll().stream().map(this::toResponse).toList();
        }

        return taskRepository.findByOwnerUsername(authentication.getName()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id, Authentication authentication) {
        Task task = loadAuthorizedTask(id, authentication);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse create(TaskRequest request, Authentication authentication) {
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description().trim());
        task.setStatus(request.status() == null ? TaskStatus.OPEN : request.status());
        task.setOwnerUsername(authentication.getName());
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        Task saved = taskRepository.save(task);
        auditTrailService.record(
                "TASK_CREATED",
                authentication.getName(),
                "TASK",
                String.valueOf(saved.getId()),
                saved.getTitle());
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request, Authentication authentication) {
        Task task = loadAuthorizedTask(id, authentication);
        task.setTitle(request.title().trim());
        task.setDescription(request.description().trim());
        task.setStatus(request.status());
        task.setUpdatedAt(Instant.now());

        Task saved = taskRepository.save(task);
        auditTrailService.record(
                "TASK_UPDATED",
                authentication.getName(),
                "TASK",
                String.valueOf(saved.getId()),
                saved.getTitle());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Task task = loadAuthorizedTask(id, authentication);
        auditTrailService.record(
                "TASK_DELETED",
                authentication.getName(),
                "TASK",
                String.valueOf(task.getId()),
                task.getTitle());
        taskRepository.delete(task);
    }

    private Task loadAuthorizedTask(Long id, Authentication authentication) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task was not found"));

        if (!isAdmin(authentication) && !task.getOwnerUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's task");
        }

        return task;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getOwnerUsername(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
