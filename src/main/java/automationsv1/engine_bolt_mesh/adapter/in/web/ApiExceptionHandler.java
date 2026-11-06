package automationsv1.engine_bolt_mesh.adapter.in.web;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.exception.WorkflowExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail notFound(ResourceNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(WorkflowExecutionException.class)
    public ProblemDetail executionError(WorkflowExecutionException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

}
