package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.neo4j.OrderNode;
import com.example.nordicelectronics.service.neo4j.OrderNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Order Controller", description = "Handles operations related to orders in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/orders")
public class OrderNeo4jController {

    private final OrderNeo4jService orderNeo4jService;

    @Operation(summary = "Get all Neo4j orders")
    @GetMapping("")
    public ResponseEntity<List<OrderNode>> getAll() {
        return new ResponseEntity<>(orderNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j order by ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderNode> getByOrderId(@PathVariable UUID orderId) {
        return new ResponseEntity<>(orderNeo4jService.getByOrderId(orderId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j orders by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderNode>> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(orderNeo4jService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j orders by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderNode>> getByStatus(@PathVariable OrderStatus status) {
        return new ResponseEntity<>(orderNeo4jService.getByStatus(status), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j order")
    @PostMapping("")
    public ResponseEntity<OrderNode> save(@RequestBody OrderNode orderNode) {
        return new ResponseEntity<>(orderNeo4jService.save(orderNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j order")
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderNode> update(@PathVariable UUID orderId, @RequestBody OrderNode orderNode) {
        return new ResponseEntity<>(orderNeo4jService.update(orderId, orderNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j order")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> delete(@PathVariable UUID orderId) {
        orderNeo4jService.deleteByOrderId(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

