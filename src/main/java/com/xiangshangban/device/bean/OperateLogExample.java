package com.xiangshangban.device.bean;

import java.util.ArrayList;
import java.util.List;

public class OperateLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public OperateLogExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdIsNull() {
            addCriterion("operate_emp_id is null");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdIsNotNull() {
            addCriterion("operate_emp_id is not null");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdEqualTo(String value) {
            addCriterion("operate_emp_id =", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdNotEqualTo(String value) {
            addCriterion("operate_emp_id <>", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdGreaterThan(String value) {
            addCriterion("operate_emp_id >", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdGreaterThanOrEqualTo(String value) {
            addCriterion("operate_emp_id >=", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdLessThan(String value) {
            addCriterion("operate_emp_id <", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdLessThanOrEqualTo(String value) {
            addCriterion("operate_emp_id <=", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdLike(String value) {
            addCriterion("operate_emp_id like", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdNotLike(String value) {
            addCriterion("operate_emp_id not like", value, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdIn(List<String> values) {
            addCriterion("operate_emp_id in", values, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdNotIn(List<String> values) {
            addCriterion("operate_emp_id not in", values, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdBetween(String value1, String value2) {
            addCriterion("operate_emp_id between", value1, value2, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpIdNotBetween(String value1, String value2) {
            addCriterion("operate_emp_id not between", value1, value2, "operateEmpId");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyIsNull() {
            addCriterion("operate_emp_company is null");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyIsNotNull() {
            addCriterion("operate_emp_company is not null");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyEqualTo(String value) {
            addCriterion("operate_emp_company =", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyNotEqualTo(String value) {
            addCriterion("operate_emp_company <>", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyGreaterThan(String value) {
            addCriterion("operate_emp_company >", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyGreaterThanOrEqualTo(String value) {
            addCriterion("operate_emp_company >=", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyLessThan(String value) {
            addCriterion("operate_emp_company <", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyLessThanOrEqualTo(String value) {
            addCriterion("operate_emp_company <=", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyLike(String value) {
            addCriterion("operate_emp_company like", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyNotLike(String value) {
            addCriterion("operate_emp_company not like", value, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyIn(List<String> values) {
            addCriterion("operate_emp_company in", values, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyNotIn(List<String> values) {
            addCriterion("operate_emp_company not in", values, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyBetween(String value1, String value2) {
            addCriterion("operate_emp_company between", value1, value2, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateEmpCompanyNotBetween(String value1, String value2) {
            addCriterion("operate_emp_company not between", value1, value2, "operateEmpCompany");
            return (Criteria) this;
        }

        public Criteria andOperateTypeIsNull() {
            addCriterion("operate_type is null");
            return (Criteria) this;
        }

        public Criteria andOperateTypeIsNotNull() {
            addCriterion("operate_type is not null");
            return (Criteria) this;
        }

        public Criteria andOperateTypeEqualTo(String value) {
            addCriterion("operate_type =", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeNotEqualTo(String value) {
            addCriterion("operate_type <>", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeGreaterThan(String value) {
            addCriterion("operate_type >", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeGreaterThanOrEqualTo(String value) {
            addCriterion("operate_type >=", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeLessThan(String value) {
            addCriterion("operate_type <", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeLessThanOrEqualTo(String value) {
            addCriterion("operate_type <=", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeLike(String value) {
            addCriterion("operate_type like", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeNotLike(String value) {
            addCriterion("operate_type not like", value, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeIn(List<String> values) {
            addCriterion("operate_type in", values, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeNotIn(List<String> values) {
            addCriterion("operate_type not in", values, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeBetween(String value1, String value2) {
            addCriterion("operate_type between", value1, value2, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateTypeNotBetween(String value1, String value2) {
            addCriterion("operate_type not between", value1, value2, "operateType");
            return (Criteria) this;
        }

        public Criteria andOperateContentIsNull() {
            addCriterion("operate_content is null");
            return (Criteria) this;
        }

        public Criteria andOperateContentIsNotNull() {
            addCriterion("operate_content is not null");
            return (Criteria) this;
        }

        public Criteria andOperateContentEqualTo(String value) {
            addCriterion("operate_content =", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentNotEqualTo(String value) {
            addCriterion("operate_content <>", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentGreaterThan(String value) {
            addCriterion("operate_content >", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentGreaterThanOrEqualTo(String value) {
            addCriterion("operate_content >=", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentLessThan(String value) {
            addCriterion("operate_content <", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentLessThanOrEqualTo(String value) {
            addCriterion("operate_content <=", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentLike(String value) {
            addCriterion("operate_content like", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentNotLike(String value) {
            addCriterion("operate_content not like", value, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentIn(List<String> values) {
            addCriterion("operate_content in", values, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentNotIn(List<String> values) {
            addCriterion("operate_content not in", values, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentBetween(String value1, String value2) {
            addCriterion("operate_content between", value1, value2, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateContentNotBetween(String value1, String value2) {
            addCriterion("operate_content not between", value1, value2, "operateContent");
            return (Criteria) this;
        }

        public Criteria andOperateDateIsNull() {
            addCriterion("operate_date is null");
            return (Criteria) this;
        }

        public Criteria andOperateDateIsNotNull() {
            addCriterion("operate_date is not null");
            return (Criteria) this;
        }

        public Criteria andOperateDateEqualTo(String value) {
            addCriterion("operate_date =", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateNotEqualTo(String value) {
            addCriterion("operate_date <>", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateGreaterThan(String value) {
            addCriterion("operate_date >", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateGreaterThanOrEqualTo(String value) {
            addCriterion("operate_date >=", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateLessThan(String value) {
            addCriterion("operate_date <", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateLessThanOrEqualTo(String value) {
            addCriterion("operate_date <=", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateLike(String value) {
            addCriterion("operate_date like", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateNotLike(String value) {
            addCriterion("operate_date not like", value, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateIn(List<String> values) {
            addCriterion("operate_date in", values, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateNotIn(List<String> values) {
            addCriterion("operate_date not in", values, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateBetween(String value1, String value2) {
            addCriterion("operate_date between", value1, value2, "operateDate");
            return (Criteria) this;
        }

        public Criteria andOperateDateNotBetween(String value1, String value2) {
            addCriterion("operate_date not between", value1, value2, "operateDate");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}