package automationsv1.engine_bolt_mesh.adapter.out.persistence.repository;

import automationsv1.engine_bolt_mesh.adapter.out.persistence.entity.ApiDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiDefinitionRepository extends JpaRepository<ApiDefinitionEntity, UUID> {
}
