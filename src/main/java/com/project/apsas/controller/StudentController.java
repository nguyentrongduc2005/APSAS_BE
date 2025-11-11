package com.project.apsas.controller;

import com.project.apsas.dto.student.StudentRequest;
import com.project.apsas.dto.student.StudentResponse;
import com.project.apsas.entity.Student;
import com.project.apsas.mapper.StudentMapper;
import com.project.apsas.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository repo;
    private final StudentMapper mapper;

    public StudentController(StudentRepository repo, StudentMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest req) {
        // nếu cần ràng buộc email duy nhất
        if (req.getEmail() != null && repo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        Student entity = mapper.toEntity(req);
        entity = repo.save(entity);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<StudentResponse>> list(Pageable pageable) {
        Page<StudentResponse> page = repo.findAll(pageable).map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody StudentRequest req) {
        return repo.findById(id)
                .map(entity -> {
                    mapper.updateEntity(entity, req);
                    return ResponseEntity.ok(mapper.toResponse(repo.save(entity)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
