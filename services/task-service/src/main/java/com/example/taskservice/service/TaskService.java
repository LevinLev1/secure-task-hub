package com.example.taskservice.service;

import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.model.TaskItem;
import com.example.taskservice.model.TaskStatus;
import com.example.taskservice.observability.AuditTrailService;
import com.example.taskservice.repository.TaskRepository;
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
        TaskItem taskItem = loadAuthorizedTask(id, authentication);
        return toResponse(taskItem);
    }

    @Transactional
    public TaskResponse create(TaskRequest request, Authentication authentication) {
        TaskItem taskItem = new TaskItem();
        taskItem.setTitle(request.title().trim());
        taskItem.setDescription(request.description().trim());
        taskItem.setStatus(request.status() == null ? TaskStatus.OPEN : request.status());
        taskItem.setOwnerUsername(authentication.getName());
        taskItem.setCreatedAt(Instant.now());
        taskItem.setUpdatedAt(Instant.now());

        TaskItem saved = taskRepository.save(taskItem);
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
        TaskItem taskItem = loadAuthorizedTask(id, authentication);
        taskItem.setTitle(request.title().trim());
        taskItem.setDescription(request.description().trim());
        taskItem.setStatus(request.status());
        taskItem.setUpdatedAt(Instant.now());

        TaskItem saved = taskRepository.save(taskItem);
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
        TaskItem taskItem = loadAuthorizedTask(id, authentication);
        auditTrailService.record(
                "TASK_DELETED",
                authentication.getName(),
                "TASK",
                String.valueOf(taskItem.getId()),
                taskItem.getTitle());
        taskRepository.delete(taskItem);
    }

    private TaskItem loadAuthorizedTask(Long id, Authentication authentication) {
        TaskItem taskItem = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task was not found"));

        if (!isAdmin(authentication) && !taskItem.getOwnerUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's task");
        }

        return taskItem;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private TaskResponse toResponse(TaskItem taskItem) {
        return new TaskResponse(
                taskItem.getId(),
                taskItem.getTitle(),
                taskItem.getDescription(),
                taskItem.getStatus(),
                taskItem.getOwnerUsername(),
                taskItem.getCreatedAt(),
                taskItem.getUpdatedAt()
        );
    }
}
