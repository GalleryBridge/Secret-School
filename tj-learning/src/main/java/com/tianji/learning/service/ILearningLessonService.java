package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;

import java.util.List;

/**
 * <p>
 * 学生课程表 服务类
 * </p>
 *
 * @author 东哥
 * @since 2023-05-21
 */
public interface ILearningLessonService extends IService<LearningLesson> {
    /**
     * 1、添加课表 （Day2 - 随堂）
     * @param userId 用户ID
     * @param courseIds 课程ID集合
     */
    void addUserLessons(Long userId, List<Long> courseIds);

    /**
     * 2、分页查询我的课表 （Day2 - 随堂）
     * @param query
     * @return
     */
    PageDTO<LearningLessonVO> queryMyLessons(PageQuery query);

    /**
     * 3、查询我正在学习的课程（Day2 - 作业）
     * @return
     */
    LearningLessonVO queryMyCurrentLesson();

    /**
     * 4、查询指定课程信息（Day2 - 作业）
     * @param courseId
     * @return
     */
    LearningLessonVO queryLessonByCourseId(Long courseId);

    /**
     * 5、删除指定课程信息（Day2 - 作业）
     * @param userId
     * @param courseId
     */
    void deleteCourseFromLesson(Long userId, Long courseId);

    /**
     * 6、统计课程学习人数（Day2 - 作业）
     * @param courseId
     * @return
     */
    Integer countLearningLessonByCourse(Long courseId);

    /**
     * 7、校验当前课程是否已经报名（Day2 - 作业）
     * @param courseId
     * @return
     */
    Long isLessonValid(Long courseId);

    /**
     * 8、根据用户ID和课程ID查询  ==> 课表信息 （Day3 - 随堂）
     * @param userId
     * @param courseId
     * @return
     */
    LearningLesson queryByUserAndCourseId(Long userId, Long courseId);

    /**
     * 9、创建学习计划 （Day3 - 随堂）
     * @param courseId 课程ID
     * @param freq 学习频率
     */
    void createLearningPlan(Long courseId, Integer freq);

    /**
     * 10、统计我的学习计划 （Day3 - 随堂）
     * @param query
     * @return
     */
    LearningPlanPageVO queryMyPlans(PageQuery query);
}
