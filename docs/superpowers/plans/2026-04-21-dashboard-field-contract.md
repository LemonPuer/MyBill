# Dashboard Field Contract Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename the Dashboard display-field contract so budget and consumption responses use explicit field names without changing existing statistics logic.

**Architecture:** Keep the existing AppController endpoints and service aggregation logic, but rename response DTO fields from ambiguous display names to explicit names. Only touch the two response models and the service mapping code that populates them, then update the backend task document to reflect the finalized contract.

**Tech Stack:** Spring Boot, MyBatis-Flex, Lombok, Maven, Markdown docs

---

## File Map

- Modify: `src/main/java/org/lemon/entity/resp/BudgetInfoVO.java`
  - Rename `category` to `categoryName` and `cost` to `spent`.
- Modify: `src/main/java/org/lemon/entity/resp/ConsumptionStatisticsVO.java`
  - Rename `category` to `categoryName`.
- Modify: `src/main/java/org/lemon/service/BudgetService.java`
  - Populate the renamed budget response fields.
- Modify: `src/main/java/org/lemon/service/FinanceTransactionsService.java`
  - Populate the renamed consumption statistics field.
- Modify: `docs/superpowers/2026-04-21-backend-task-document.md`
  - Mark Dashboard field contract as finalized with the new names.

## Task 1: Rename budget response fields

**Files:**
- Modify: `src/main/java/org/lemon/entity/resp/BudgetInfoVO.java`
- Modify: `src/main/java/org/lemon/service/BudgetService.java`

- [ ] **Step 1: Rename the DTO fields in `BudgetInfoVO`**

```java
    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 已花费金额
     */
    private String spent;
```

- [ ] **Step 2: Update the service mapping in `BudgetService#getBudgetInfo`**

```java
        return list.stream().map(o -> {
            Category category = categoryMap.getOrDefault(o.getCategoryId(), new Category());
            return BudgetInfoVO.builder()
                    .id(o.getId())
                    .icon(category == null ? "" : category.getIcon())
                    .amount(o.getAmount().toString())
                    .categoryName(category == null ? "" : category.getCategory())
                    .spent(expenseMap.getOrDefault(o.getCategoryId(), 0.0).toString())
                    .startTime(o.getStartTime())
                    .endTime(o.getEndTime())
                    .build();
        }).collect(Collectors.toList());
```

## Task 2: Rename consumption statistics display field

**Files:**
- Modify: `src/main/java/org/lemon/entity/resp/ConsumptionStatisticsVO.java`
- Modify: `src/main/java/org/lemon/service/FinanceTransactionsService.java`

- [ ] **Step 1: Rename the DTO field in `ConsumptionStatisticsVO`**

```java
    /**
     * 分类名称
     */
    private String categoryName;
```

- [ ] **Step 2: Update the service mapping in `FinanceTransactionsService#getConsumptionStatistics`**

```java
        collect.forEach((key, value) -> {
            ConsumptionStatisticsVO vo = new ConsumptionStatisticsVO();
            vo.setCategoryName(categoryMap.getOrDefault(key, ""));
            vo.setConsumption(value);
            result.add(vo);
        });
```

## Task 3: Update task document and verify build

**Files:**
- Modify: `docs/superpowers/2026-04-21-backend-task-document.md`

- [ ] **Step 1: Update the backend task document to reflect the finalized names**

```md
- `src/main/java/org/lemon/entity/resp/BudgetInfoVO.java`
  - 当前字段定稿为：`categoryName`、`icon`、`amount`、`spent`

- `src/main/java/org/lemon/entity/resp/ConsumptionStatisticsVO.java`
  - 当前字段定稿为：`categoryName`、`consumption`
```

- [ ] **Step 2: Run full project verification**

Run: `mvn test`

Expected:
- Build succeeds
- Existing tests remain green
- No additional runtime-only changes are required for this contract rename
