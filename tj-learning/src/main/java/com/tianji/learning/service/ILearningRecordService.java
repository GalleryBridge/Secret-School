package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningRecord;

/**
 * <p>
 * 学习记录表 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-12-10
 */
public interface ILearningRecordService extends IService<LearningRecord> {

    /**
     * 1、查询指定课程的学习记录（Day3 - 随堂）
     * @param courseId
     * @return
     */
    LearningLessonDTO queryLearningRecordByCourse(Long courseId);

    /**
     * 2、提交学习记录 （Day3 - 随堂）
     * @param formDTO
     */
    void addLearningRecord(LearningRecordFormDTO formDTO);
}
