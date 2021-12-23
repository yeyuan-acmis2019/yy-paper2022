package org.jeecg.modules.demo.emergencycompile.mapper;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.demo.emergencycompile.entity.Emergency;
import org.jeecg.modules.demo.emergencycompile.entity.Relation;
import org.jeecg.modules.demo.emergencycompile.entity.Task;

import java.util.List;
import java.util.Map;

public interface CompileMapper {

    void writeEmergency(Emergency em);

    List<Map<String, Object>> getEmergencyList();

    void writeTask(Task task);

    void writeEmergencyTaskRelation(Relation r);

    List<String> getTaskList(@Param("id") int id);
}