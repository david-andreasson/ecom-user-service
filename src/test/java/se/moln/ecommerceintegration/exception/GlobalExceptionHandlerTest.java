package se.moln.ecommerceintegration.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_masksPasswordRejectedValue_andBuildsDetails() {
        class Target { String firstName; String password; }
        Target t = new Target();
        BindException be = new BindException(t, "target");
        be.addError(new FieldError("target", "firstName", "Anna" , false, new String[]{"Size"}, null, "First bad"));
        be.addError(new FieldError("target", "password", "Secret123!" , false, new String[]{"Pattern"}, null, "Password bad"));

        var res = handler.handleValidation(be);
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        var body = (java.util.Map<?,?>) res.getBody();
        assertThat(body.get("error")).isEqualTo("validation_failed");
        List<?> details = (List<?>) body.get("details");
        assertThat(details).hasSize(2);
        var passDetail = (java.util.Map<?,?>) details.get(1);
        assertThat(passDetail.get("field")).isEqualTo("password");
        assertThat(passDetail.get("rejectedValue")).isNull();
    }

    @Test
    void handleNotReadable_returnsBadRequest() {
        var res = handler.handleNotReadable(new HttpMessageNotReadableException("bad json"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        var body = (java.util.Map<?,?>) res.getBody();
        assertThat(body.get("error")).isEqualTo("bad_request");
    }

    @Test
    void handleTypeMismatch_returnsValidationFailed() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) Integer.class);
        when(ex.getValue()).thenReturn("abc");

        var res = handler.handleTypeMismatch(ex);
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        var body = (java.util.Map<?,?>) res.getBody();
        assertThat(body.get("error")).isEqualTo("validation_failed");
    }

    @Test
    void handleConstraintViolation_truncatesLongRejectedValue() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> v = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        // simple iterator mock that returns empty iterator; toString will be null, so we stub toString
        when(path.iterator()).thenReturn(java.util.Collections.emptyIterator());
        when(path.toString()).thenReturn("field");
        when(v.getPropertyPath()).thenReturn(path);
        when(v.getMessage()).thenReturn("bad");
        var ann = mock(Annotation.class);
        var desc = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        when(desc.getAnnotation()).thenReturn(ann);
        when(ann.annotationType()).thenReturn((Class) DummyConstraint.class);
        when(v.getConstraintDescriptor()).thenReturn(desc);
        when(v.getInvalidValue()).thenReturn("x".repeat(250));

        var res = handler.handleConstraintViolation(new ConstraintViolationException(Set.of(v)));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        var body = (java.util.Map<?,?>) res.getBody();
        List<?> details = (List<?>) body.get("details");
        var detail = (java.util.Map<?,?>) details.get(0);
        String rv = (String) detail.get("rejectedValue");
        assertThat(rv).hasSizeGreaterThan(200);
        assertThat(rv).endsWith("...");
    }

    @interface DummyConstraint { }

    @Test
    void handleResponseStatus_usesStatusAndReason() {
        var res = handler.handleResponseStatus(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        assertThat(res.getStatusCode().value()).isEqualTo(401);
        var body = (java.util.Map<?,?>) res.getBody();
        assertThat(body.get("error")).isEqualTo("unauthorized");
        assertThat(body.get("message")).isEqualTo("Invalid credentials");
    }

    @Test
    void handleAccessDenied_returns403() {
        var res = handler.handleAccessDenied(new org.springframework.security.access.AccessDeniedException("nope"));
        assertThat(res.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void handleNotFound_returns404() {
        var res = handler.handleNotFound(new java.util.NoSuchElementException("missing"));
        assertThat(res.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void handleIllegalState_returns409() {
        var res = handler.handleIllegalState(new IllegalStateException("dup"));
        assertThat(res.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void handleGeneric_returns500() {
        var res = handler.handleGeneric(new RuntimeException("boom"));
        assertThat(res.getStatusCode().value()).isEqualTo(500);
    }
}
