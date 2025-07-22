import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Button,
  Grid,
  SelectChangeEvent,
} from '@mui/material';
import { FilterOptions, FilterState } from '../types';

interface FilterPanelProps {
  options: FilterOptions;
  filters: FilterState;
  onFilterChange: (filters: FilterState) => void;
  onApplyFilters: () => void;
  onResetFilters: () => void;
}

const FilterPanel: React.FC<FilterPanelProps> = ({
  options,
  filters,
  onFilterChange,
  onApplyFilters,
  onResetFilters,
}) => {
  const handleFilterChange = (field: keyof FilterState) => (
    event: SelectChangeEvent<string | number>
  ) => {
    const value = event.target.value;
    onFilterChange({
      ...filters,
      [field]: value === '' ? undefined : value,
    });
  };

  const getActiveFiltersCount = (): number => {
    return Object.values(filters).filter(value => value !== undefined && value !== '').length;
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" component="h2">
            필터
          </Typography>
          {getActiveFiltersCount() > 0 && (
            <Chip 
              label={`${getActiveFiltersCount()}개 활성`} 
              color="primary" 
              size="small" 
            />
          )}
        </Box>

        <Grid container spacing={2}>
          {/* 유튜버 선택 */}
          <Grid item xs={12} md={3}>
            <FormControl fullWidth size="small">
              <InputLabel>유튜버</InputLabel>
              <Select
                value={filters.selectedUserId || ''}
                label="유튜버"
                onChange={handleFilterChange('selectedUserId')}
              >
                <MenuItem value="">전체</MenuItem>
                {options.users.map((user) => (
                  <MenuItem key={user.id} value={user.id}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {user.profileImageUrl && (
                        <img
                          src={user.profileImageUrl}
                          alt={user.name}
                          style={{ width: 24, height: 24, borderRadius: '50%' }}
                        />
                      )}
                      <span>{user.name}</span>
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* 국가 선택 */}
          <Grid item xs={12} md={3}>
            <FormControl fullWidth size="small">
              <InputLabel>국가</InputLabel>
              <Select
                value={filters.selectedCountryCode || ''}
                label="국가"
                onChange={handleFilterChange('selectedCountryCode')}
              >
                <MenuItem value="">전체</MenuItem>
                {options.countries.map((country) => (
                  <MenuItem key={country} value={country}>
                    {country}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>



          {/* 연도 선택 */}
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>연도</InputLabel>
              <Select
                value={filters.selectedYear || ''}
                label="연도"
                onChange={handleFilterChange('selectedYear')}
              >
                <MenuItem value="">전체</MenuItem>
                {options.years.map((year) => (
                  <MenuItem key={year} value={year}>
                    {year}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* 대륙 선택 */}
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>대륙</InputLabel>
              <Select
                value={filters.selectedContinent || ''}
                label="대륙"
                onChange={handleFilterChange('selectedContinent')}
              >
                <MenuItem value="">전체</MenuItem>
                {options.continents.map((continent) => (
                  <MenuItem key={continent} value={continent}>
                    {continent}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>

        {/* 버튼 영역 */}
        <Box sx={{ display: 'flex', gap: 2, mt: 2, justifyContent: 'flex-end' }}>
          <Button
            variant="outlined"
            onClick={onResetFilters}
            disabled={getActiveFiltersCount() === 0}
          >
            초기화
          </Button>
          <Button
            variant="contained"
            onClick={onApplyFilters}
            disabled={getActiveFiltersCount() === 0}
          >
            적용
          </Button>
        </Box>

        {/* 활성 필터 태그들 */}
        {getActiveFiltersCount() > 0 && (
          <Box sx={{ mt: 2 }}>
            <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
              활성 필터:
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {filters.selectedUserId && (
                <Chip
                  label={`유튜버: ${options.users.find(u => u.id === filters.selectedUserId)?.name}`}
                  size="small"
                  onDelete={() => onFilterChange({ ...filters, selectedUserId: undefined })}
                />
              )}
              {filters.selectedCountryCode && (
                <Chip
                  label={`국가: ${filters.selectedCountryCode}`}
                  size="small"
                  onDelete={() => onFilterChange({ ...filters, selectedCountryCode: undefined })}
                />
              )}

              {filters.selectedYear && (
                <Chip
                  label={`연도: ${filters.selectedYear}`}
                  size="small"
                  onDelete={() => onFilterChange({ ...filters, selectedYear: undefined })}
                />
              )}
              {filters.selectedContinent && (
                <Chip
                  label={`대륙: ${filters.selectedContinent}`}
                  size="small"
                  onDelete={() => onFilterChange({ ...filters, selectedContinent: undefined })}
                />
              )}
            </Box>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default FilterPanel; 