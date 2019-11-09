package com.jpa.quartz.dao;

import com.jpa.quartz.entity.JobEntity;
import org.springframework.data.repository.CrudRepository;
/**
 * Created by Hens
 */
public interface JobEntityRepository extends CrudRepository<JobEntity, Long> {
    JobEntity getById(Integer id);
}
