import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { authAPI } from '../api';

export function useAuth() {
    const queryClient = useQueryClient();

    // Query for current user
    const { data: authData, isLoading } = useQuery({
        queryKey: ['auth', 'current-user'],
        queryFn: () => authAPI.getCurrentUser(),
        retry: false,
        staleTime: 5 * 60 * 1000, // 5 minutes
    });

    const currentUser = authData?.user && authData?.isAuthenticated ? authData.user : null;

    // Login mutation
    const loginMutation = useMutation({
        mutationFn: ({ email, password }) => authAPI.login(email, password),
        onSuccess: (data) => {
            if (data.success) {
                queryClient.setQueryData(['auth', 'current-user'], { user: data.user, isAuthenticated: true });
                queryClient.invalidateQueries({ queryKey: ['auth', 'current-user'] });
            }
        },
    });

    // Register mutation
    const registerMutation = useMutation({
        mutationFn: (userData) => authAPI.register(userData),
    });

    // Logout mutation
    const logoutMutation = useMutation({
        mutationFn: () => authAPI.logout(),
        onSuccess: () => {
            queryClient.setQueryData(['auth', 'current-user'], { user: null, isAuthenticated: false });
            queryClient.invalidateQueries({ queryKey: ['auth', 'current-user'] });
        },
    });

    return {
        currentUser,
        isLoading,
        isAuthenticated: !!currentUser,
        login: loginMutation.mutate,
        loginAsync: loginMutation.mutateAsync,
        isLoggingIn: loginMutation.isPending,
        register: registerMutation.mutate,
        registerAsync: registerMutation.mutateAsync,
        isRegistering: registerMutation.isPending,
        logout: logoutMutation.mutate,
        logoutAsync: logoutMutation.mutateAsync,
        isLoggingOut: logoutMutation.isPending,
        loginError: loginMutation.error,
        registerError: registerMutation.error,
    };
}

