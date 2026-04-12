package com.example.taskservice.repository;

import com.example.taskservice.model.TaskItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskItem, Long> {

    List<TaskItem> findByOwnerUsername(String ownerUsername);
}
