package com.securetaskhub.task.repository;

import com.securetaskhub.task.model.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerUsername(String ownerUsername);
}
