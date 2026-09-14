package com.bookstore.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * REST controller for health check and monitoring endpoints.
 * <p>
 * Provides a simple health check endpoint to verify the application is running.
 * This is a public endpoint accessible without authentication, typically used
 * by load balancers and monitoring systems.
 * </p>
 */
@RestController
@RequestMapping
@Tag(name = "Health Check", description = "APIs for application health monitoring")
public class HealthController {

    /**
     * Health check endpoint to verify application availability.
     * <p>
     * Returns the current server timestamp along with an OK status message.
     * This endpoint is used by monitoring tools to check if the service is running.
     * </p>
     *
     * @return timestamp and OK status message as plain text
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifies the application is running and responsive")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application is healthy and running",
                    content = @Content
            )
    })
    public String getHealth() {
        return new Date() + ": OK";
    }
}
