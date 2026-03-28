## Context

The BPMN and CMMN diagram viewers (`BpmnViewer.tsx`, `CmmnViewer.tsx`) already receive `completedElementIds` arrays from the backend diagram API. They currently apply overlays for `activeElementIds` (orange highlight) and `currentElementId` (animated pulse). Completed elements are listed in the spec requirements but receive no distinct visual treatment in the actual rendering.

Both viewers use the bpmn-js / cmmn-js `Overlays` API to add CSS-based overlays on diagram elements.

## Goals / Non-Goals

**Goals:**
- Visually differentiate completed elements with a distinct green styling and checkmark indicator
- Keep completed, active, and current highlights visually distinct (green, orange, blue pulse)
- Add a compact legend below each diagram showing what each color means
- Apply consistently to both BPMN and CMMN viewers

**Non-Goals:**
- Backend changes — `completedElementIds` is already computed and returned
- Changes to diagram navigation (zoom/pan/fit)
- Historic process instance replay or timeline visualization
- Changing the overlay system architecture (continue using bpmn-js/cmmn-js Overlays)

- Click-through interactions on completed elements

## Decisions

### 1. Completed element styling: green border + checkmark badge

**Choice**: Add a green (#22c55e) border/outline overlay with a small checkmark badge on completed elements.

**Rationale**: Green universally signals "done/completed". A checkmark badge provides a clear icon even at small zoom levels. The border is lightweight and doesn't obstruct the element shape.

**Alternative considered**: Full background fill — too visually heavy, obscures the element content.

### 2. Shared overlay utility

**Choice**: Create a shared `useDiagramHighlights` hook/utility used by both BPMN and CMMN viewers rather than duplicating overlay logic.

**Rationale**: Both viewers use identical overlay patterns (add markers for completed, active, current). A shared utility reduces code duplication and ensures consistent styling.

**Alternative considered**: Inline per viewer — simpler but duplicates overlay setup code across 4 files.

### 3. Legend component

**Choice**: Small inline legend bar below the diagram canvas showing colored dots with labels (Completed/Active/Current).

**Rationale**: Users need to understand the visual language without external documentation. Inline placement keeps it legend in context.

## Risks / Trade-offs

- **Overlay performance with many elements** → Mitigated by using CSS classes rather than SVG overlays where possible; Flowable's Overlays API handles this well
- **Element ID mismatches between plan items and diagram shapes** → Mitigated by matching both `elementId` and `planItemId` variants (already handled in CMMN viewer)
- **Color blindness** → Green/orange contrast is sufficient for most color vision deficiencies; could add pattern fills as enhancement if requested
