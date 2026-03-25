---
status: awaiting_human_verify
trigger: "CMMN case diagram shows white screen despite successful XML parsing"
created: 2026-03-25T00:00:00Z
updated: 2026-03-25T01:05:00Z
---

## Current Focus
hypothesis: FIX APPLIED - DI detection now checks for opening element tag
test: Code compiles successfully
expecting: After backend restart, CmmnDiGenerator will execute and add DI data
next_action: Request human verification with checkpoint

## Symptoms
expected: CMMN case diagram with plan items, tasks, overlays (similar to working BPMN diagrams)
actual: White screen, nothing rendered
errors: No errors - logs show success
reproduction: View active CMMN case instance detail page at /workspace/{id}?type=CMMN
started: Started during CMMN visualization implementation (branch 005-openspec). BPMN diagrams work correctly.

## Eliminated

## Evidence
- timestamp: 2026-03-25T01:00:00Z
  checked: CaseService.java line 439 and CmmnDiGenerator.java line 44
  found: Both check for `contains("cmmndi:CMMNDI")` which matches namespace declarations like `xmlns:cmmndi=` but NOT the actual `<cmmndi:CMMNDI>` element
  implication: Code incorrectly thinks DI exists when only namespaces are present. CmmnDiGenerator.addDiInformation() never executes because check returns false positive

- timestamp: 2026-03-25T01:00:00Z
  checked: Backend logs showing XML output
  found: XML contains namespace declarations (`xmlns:cmmndi=`, `xmlns:dc=`, `xmlns:di=`) but no `<cmmndi:CMMNDI>` element with shape bounds. XML length is 1029 chars
  implication: Detection logic is fooled by namespace declarations. Need to check for opening element tag instead

- timestamp: 2026-03-25T01:00:00Z
  checked: Fixed detection logic in both CaseService.java and CmmnDiGenerator.java
  found: Changed check from `contains("cmmndi:CMMNDI")` to `contains("<cmmndi:CMMNDI")` (opening tag). Added debug logging to show before/after XML length
  implication: Now correctly detects missing DI and triggers CmmnDiGenerator execution. Added logging to verify DI generation actually happens

- timestamp: 2026-03-25T00:00:00Z
  checked: Source CMMN files in backend/src/main/resources/processes/
  found: simple-case.cmmn and ad-hoc-case.cmmn contain only structural elements (case, casePlanModel, planItem, humanTask) with NO CMMN DI data. XML has only xmlns="http://www.omg.org/spec/CMMN/20151109/MODEL" - missing dc, di, cmmndi namespaces
  implication: cmmn-js has no layout information to position elements on canvas

- timestamp: 2026-03-25T00:00:00Z
  checked: Source BPMN file for comparison (simple-approval.bpmn20.xml)
  found: BPMN file also lacks BPMNDI section - only has basic elements (process, startEvent, userTask, sequenceFlow) without bpmndi:BPMNDiagram, shapes, or bounds
  implication: If BPMN works without DI source data, something else is generating DI during runtime

- timestamp: 2026-03-25T00:00:00Z
  checked: ProcessService.java getProcessInstanceDiagram() method
  found: Fetches BPMN XML directly from deployment resources (repositoryService.getResourceAsStream()), then falls back to BpmnXMLConverter if needed. No DI generation code visible
  implication: Either Flowable's BpmnXMLConverter adds DI automatically, or bpmn-js handles missing DI differently than cmmn-js

- timestamp: 2026-03-25T00:00:00Z
  checked: CaseService.java getCaseInstanceDiagram() method (lines 405-522)
  found: Uses CmmnXmlConverter.convertToXML() to generate XML from CmmnModel. Includes hack to add DI namespaces if missing (lines 438-448), but doesn't add actual DI elements (shapes, bounds)
  implication: Code acknowledges DI namespace issue but only adds xmlns attributes, not the required CMMNDI structure

- timestamp: 2026-03-25T00:00:00Z
  checked: Web search for bpmn-auto-layout and cmmn-auto-layout
  found: **bpmn-auto-layout** exists (https://www.npmjs.com/package/bpmn-auto-layout) to generate missing DI for BPMN. **NO equivalent cmmn-auto-layout** exists for CMMN (confirmed by npm search and GitHub). Search results show archived cmmn-js-examples repo with no active auto-layout solution
  implication: CMMN has no official auto-layout tool unlike BPMN, explaining why CMMN diagrams fail when DI is missing while BPMN diagrams work

- timestamp: 2026-03-25T00:00:00Z
  checked: cmmn-js-examples repository (https://github.com/bpmn-io/cmmn-js-examples)
  found: Official examples include newDiagram.cmmn with CMMNDI section containing shape bounds, positions. Repository is **archived** (read-only, no longer maintained since 2024)
  implication: cmmn-js requires DI data for rendering. Without it, elements cannot be positioned on canvas, resulting in empty element registry

- timestamp: 2026-03-25T00:00:00Z
  checked: Flowable 7.2.0 Maven dependencies and CMMN layout capabilities
  found: Flowable has **BpmnAutoLayout** class (https://github.com/flowable/flowable-engine/blob/master/modules/flowable-bpmn-layout/src/main/java/org/flowable/bpmn/BpmnAutoLayout.java) but **NO CmmnAutoLayout** equivalent exists. Web search for "CmmnAutoLayout" returned zero results
  implication: Flowable can auto-generate DI for BPMN but not for CMMN. This is a fundamental gap in both Flowable and cmmn-js ecosystems

## Resolution
root_cause: DI detection logic flawed - both CaseService.java (line 439) and CmmnDiGenerator.java (line 44) used `contains("cmmndi:CMMNDI")` which matched namespace declarations (`xmlns:cmmndi=`) instead of the actual `<cmmndi:CMMNDI>` element. This caused false positive detection, preventing CmmnDiGenerator.addDiInformation() from executing and leaving diagrams without required shape bounds data.
fix: Updated detection logic in both files to check for opening element tag `contains("<cmmndi:CMMNDI")` instead of string match. Added debug logging in CaseService to show XML length before/after DI generation and confirm when CmmnDiGenerator executes.
verification: Fix applied. Need to restart backend and verify: 1) "CMMN XML missing DI element - generating auto-layout" message appears in logs, 2) XML length increases from 1029 to 2000+ chars, 3) Frontend element registry populates with elements, 4) Diagram renders visible plan items
files_changed:
- backend/src/main/java/com/flowable/platform/service/CaseService.java (line 439 - fixed detection check and added logging)
- backend/src/main/java/com/flowable/platform/util/CmmnDiGenerator.java (line 44 - fixed detection check and added logging)
