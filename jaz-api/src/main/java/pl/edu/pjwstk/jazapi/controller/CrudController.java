package pl.edu.pjwstk.jazapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pl.edu.pjwstk.jazapi.model.PageInfo;
import pl.edu.pjwstk.jazapi.service.CrudService;
import pl.edu.pjwstk.jazapi.service.DbEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CrudController<T extends DbEntity> {
    @Autowired
    private final CrudService<T> service;

    public CrudController(CrudService<T> service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<List<Map<String, Object>>>
    getAll(
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "4", name = "size") int size,
            @RequestParam(defaultValue = "asc,id", name = "sort") String sort
    ) {
        try {
            String sortDirection = sort.split(",")[0];
            String sortBy = sort.split(",")[1];

            PageInfo info = new PageInfo(
                    page,
                    size,
                    service.count(),
                    sortDirection,
                    sortBy
            );

            PageRequest request = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
            );

            var payload = service.getAll(request)
                    .map(obj -> transformToDTO().apply(obj))
                    .collect(Collectors.toList());
            payload.add(info.map());

            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable("id") Long id) {
        try {
            T obj = service.getById(id);
            Map<String, Object> payload = transformToDTO().apply(obj);

            return new ResponseEntity<>(payload, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody T t) {
        try {
            service.createOrUpdate(t);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody T t) {
        try {
            service.createOrUpdate(t);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        try {
            service.delete(id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public abstract Function<T, Map<String, Object>> transformToDTO();
}
