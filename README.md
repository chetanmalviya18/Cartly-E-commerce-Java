# Cartly E-Commerce

Cartly is a modern full-stack e-commerce platform built with a Java backend and a modular React TypeScript frontend.

---

## 🚀 Contributing to the Client (`Client/`)

All frontend client contributions must follow our feature-driven architecture conventions.

### 1. Development Setup
```bash
# Navigate to the client directory
cd Client

# Install dependencies
npm install

# Start the local development server
npm run dev

# Run TypeScript build check
npm run build
```

---

### 2. How to Add New Code

- **Adding a New Feature**: Create a folder under `src/features/<feature-name>` containing `api/`, `components/`, and `hooks/` subdirectories with a public `index.ts` export barrel.
- **Adding Shared Components**: Place presentational, domain-agnostic UI primitives in `src/components/ui/` or `src/components/layout/`.
- **Adding New Pages**: Create view page components under `src/pages/<page-name>/` and export them via `src/pages/index.ts`.
- **Global Utilities & Services**: Place helper functions in `src/utils/`, custom global hooks in `src/hooks/`, and HTTP client logic in `src/services/`.

---

### 3. Contribution Workflow
1. Ensure code passes TypeScript compilation (`npm run build`).
2. Keep feature domain logic contained within `src/features/` without direct circular feature dependencies.
3. Submit clean commits scoped to feature areas.
