package automationsv1.engine_bolt_mesh.adapter.out.persistence.repository;

import automationsv1.engine_bolt_mesh.adapter.out.persistence.entity.WorkflowDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity, UUID> {
}
