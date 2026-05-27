package springboot.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springboot.backend.entity.Task;
import springboot.backend.repository.TaskRepository;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskRepository taskrepo;

    @GetMapping
    public List<Task> getAllTasks(){
        return taskrepo.findAll();
    }

    @PostMapping
    public Task createTask(@RequestBody Task task){
        return taskrepo.save(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskdetails
){
        Task task = taskrepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTaskName(taskdetails
                .getTaskName());
        task.setDescription(taskdetails
                .getDescription());
        task.setTaskStatus(taskdetails
                .getTaskStatus());
        task.setAssignedUser(taskdetails
                .getAssignedUser());

        Task updatedTask = taskrepo.save(task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        if(taskrepo.existsById(id)){
            taskrepo.deleteById(id);
            return ResponseEntity.ok().<Void>build();
        }
        else
            return ResponseEntity.notFound().build();

    }
}
