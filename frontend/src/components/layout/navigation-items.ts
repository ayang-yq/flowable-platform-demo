import {
  Home,
  CheckSquare,
  GitBranch,
  FileText,
  BarChart3,
  Shield,
  LucideIcon,
} from 'lucide-react';

export interface NavigationItem {
  label: string;
  path: string;
  icon: LucideIcon;
  requiredRole?: string;
}

export const navigationItems: NavigationItem[] = [
  { label: 'Home', path: '/home', icon: Home },
  { label: 'Tasks', path: '/tasks', icon: CheckSquare },
  { label: 'Processes', path: '/processes', icon: GitBranch },
  { label: 'Forms', path: '/forms/builder', icon: FileText },
  { label: 'Dashboard', path: '/dashboard', icon: BarChart3 },
  { label: 'Admin', path: '/admin', icon: Shield, requiredRole: 'ADMIN' },
];
