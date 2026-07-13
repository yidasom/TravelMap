import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  TextField,
  Alert,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Collapse,
  MenuItem,
} from '@mui/material';
import {
  Add,
  Delete,
  ExpandMore,
  ExpandLess,
  Public,
} from '@mui/icons-material';
import { keywordApi } from '../services/api';
import { CountryKeyword, CityKeyword } from '../types';

const CONTINENTS = ['Asia', 'Europe', 'North America', 'South America', 'Africa', 'Oceania'];

const emptyCountryForm = { keyword: '', countryCode: '', countryName: '', continent: 'Asia', countryEmoji: '' };
const emptyCityForm = {
  keyword: '', cityName: '', latitude: '', longitude: '',
  countryCode: '', countryName: '', continent: 'Asia', countryEmoji: ''
};

const KeywordManagementPanel: React.FC = () => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [tab, setTab] = useState<'country' | 'city'>('country');
  const [countryKeywords, setCountryKeywords] = useState<CountryKeyword[]>([]);
  const [cityKeywords, setCityKeywords] = useState<CityKeyword[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [addOpen, setAddOpen] = useState(false);
  const [countryForm, setCountryForm] = useState(emptyCountryForm);
  const [cityForm, setCityForm] = useState(emptyCityForm);

  const loadKeywords = async () => {
    try {
      const [countries, cities] = await Promise.all([
        keywordApi.getCountryKeywords(),
        keywordApi.getCityKeywords(),
      ]);
      setCountryKeywords(countries);
      setCityKeywords(cities);
    } catch (err: any) {
      setError(err.message || '키워드 목록을 불러오지 못했습니다.');
    }
  };

  useEffect(() => {
    if (isExpanded) {
      loadKeywords();
    }
  }, [isExpanded]);

  const handleAddCountryKeyword = async () => {
    if (!countryForm.keyword.trim() || !countryForm.countryCode.trim() || !countryForm.countryName.trim()) {
      setError('키워드, 국가 코드, 국가명은 필수입니다.');
      return;
    }
    try {
      setError(null);
      await keywordApi.addCountryKeyword({
        keyword: countryForm.keyword.trim(),
        countryCode: countryForm.countryCode.trim().toUpperCase(),
        countryName: countryForm.countryName.trim(),
        continent: countryForm.continent,
        countryEmoji: countryForm.countryEmoji.trim() || undefined,
      });
      setCountryForm(emptyCountryForm);
      setAddOpen(false);
      await loadKeywords();
    } catch (err: any) {
      setError(err.message || '국가 키워드 추가에 실패했습니다.');
    }
  };

  const handleAddCityKeyword = async () => {
    const lat = parseFloat(cityForm.latitude);
    const lng = parseFloat(cityForm.longitude);
    if (!cityForm.keyword.trim() || !cityForm.cityName.trim() || !cityForm.countryCode.trim()
        || !cityForm.countryName.trim() || Number.isNaN(lat) || Number.isNaN(lng)) {
      setError('키워드, 도시명, 위도, 경도, 국가 코드, 국가명은 필수입니다.');
      return;
    }
    try {
      setError(null);
      await keywordApi.addCityKeyword({
        keyword: cityForm.keyword.trim(),
        cityName: cityForm.cityName.trim(),
        latitude: lat,
        longitude: lng,
        countryCode: cityForm.countryCode.trim().toUpperCase(),
        countryName: cityForm.countryName.trim(),
        continent: cityForm.continent,
        countryEmoji: cityForm.countryEmoji.trim() || undefined,
      });
      setCityForm(emptyCityForm);
      setAddOpen(false);
      await loadKeywords();
    } catch (err: any) {
      setError(err.message || '도시 키워드 추가에 실패했습니다.');
    }
  };

  const handleDeleteCountryKeyword = async (id: number) => {
    try {
      setError(null);
      await keywordApi.deleteCountryKeyword(id);
      await loadKeywords();
    } catch (err: any) {
      setError(err.message || '국가 키워드 삭제에 실패했습니다.');
    }
  };

  const handleDeleteCityKeyword = async (id: number) => {
    try {
      setError(null);
      await keywordApi.deleteCityKeyword(id);
      await loadKeywords();
    } catch (err: any) {
      setError(err.message || '도시 키워드 삭제에 실패했습니다.');
    }
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box display="flex" alignItems="center" mb={isExpanded ? 2 : 0}>
          <Public sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6" component="h2">
            국가·도시 키워드 관리
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ ml: 1.5 }}>
            (영상 제목에서 국가/도시를 못 찾았을 때 여기서 키워드를 추가하세요)
          </Typography>
          <IconButton onClick={() => setIsExpanded(!isExpanded)} sx={{ ml: 'auto' }}>
            {isExpanded ? <ExpandLess /> : <ExpandMore />}
          </IconButton>
        </Box>

        <Collapse in={isExpanded}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
              {error}
            </Alert>
          )}

          <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
            <Tabs value={tab} onChange={(_, v) => setTab(v)}>
              <Tab label={`국가 키워드 (${countryKeywords.length})`} value="country" />
              <Tab label={`도시 키워드 (${cityKeywords.length})`} value="city" />
            </Tabs>
            <Button
              variant="contained"
              size="small"
              startIcon={<Add />}
              onClick={() => setAddOpen(true)}
            >
              키워드 추가
            </Button>
          </Box>

          {tab === 'country' ? (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>키워드</TableCell>
                    <TableCell>국가</TableCell>
                    <TableCell>대륙</TableCell>
                    <TableCell align="right">삭제</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {countryKeywords.map((ck) => (
                    <TableRow key={ck.id}>
                      <TableCell>{ck.keyword}</TableCell>
                      <TableCell>{ck.countryEmoji} {ck.countryName} ({ck.countryCode})</TableCell>
                      <TableCell>{ck.continent}</TableCell>
                      <TableCell align="right">
                        <IconButton size="small" onClick={() => handleDeleteCountryKeyword(ck.id)}>
                          <Delete fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                  {countryKeywords.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="center">
                        <Typography variant="body2" color="text.secondary">등록된 국가 키워드가 없습니다.</Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>키워드</TableCell>
                    <TableCell>도시</TableCell>
                    <TableCell>국가</TableCell>
                    <TableCell align="right">삭제</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {cityKeywords.map((ck) => (
                    <TableRow key={ck.id}>
                      <TableCell>{ck.keyword}</TableCell>
                      <TableCell>{ck.cityName}</TableCell>
                      <TableCell>{ck.countryEmoji} {ck.countryName}</TableCell>
                      <TableCell align="right">
                        <IconButton size="small" onClick={() => handleDeleteCityKeyword(ck.id)}>
                          <Delete fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                  {cityKeywords.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="center">
                        <Typography variant="body2" color="text.secondary">등록된 도시 키워드가 없습니다.</Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Collapse>
      </CardContent>

      {/* 키워드 추가 다이얼로그 */}
      <Dialog open={addOpen} onClose={() => setAddOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{tab === 'country' ? '국가 키워드 추가' : '도시 키워드 추가'}</DialogTitle>
        <DialogContent>
          {tab === 'country' ? (
            <Box display="flex" flexDirection="column" gap={2} mt={1}>
              <TextField
                label="키워드 (영상 제목에 등장할 단어)"
                value={countryForm.keyword}
                onChange={(e) => setCountryForm({ ...countryForm, keyword: e.target.value })}
                placeholder="예: 방글라데시, bangladesh"
                fullWidth
                autoFocus
              />
              <TextField
                label="국가 코드 (ISO 2자리)"
                value={countryForm.countryCode}
                onChange={(e) => setCountryForm({ ...countryForm, countryCode: e.target.value })}
                placeholder="예: BD"
                fullWidth
              />
              <TextField
                label="국가명"
                value={countryForm.countryName}
                onChange={(e) => setCountryForm({ ...countryForm, countryName: e.target.value })}
                placeholder="예: 방글라데시"
                fullWidth
              />
              <TextField
                select
                label="대륙"
                value={countryForm.continent}
                onChange={(e) => setCountryForm({ ...countryForm, continent: e.target.value })}
                fullWidth
              >
                {CONTINENTS.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
              </TextField>
              <TextField
                label="국기 이모지 (선택)"
                value={countryForm.countryEmoji}
                onChange={(e) => setCountryForm({ ...countryForm, countryEmoji: e.target.value })}
                placeholder="예: 🇧🇩"
                fullWidth
              />
            </Box>
          ) : (
            <Box display="flex" flexDirection="column" gap={2} mt={1}>
              <TextField
                label="키워드 (영상 제목/썸네일에 등장할 단어)"
                value={cityForm.keyword}
                onChange={(e) => setCityForm({ ...cityForm, keyword: e.target.value })}
                placeholder="예: 치앙마이, chiang mai"
                fullWidth
                autoFocus
              />
              <TextField
                label="도시명"
                value={cityForm.cityName}
                onChange={(e) => setCityForm({ ...cityForm, cityName: e.target.value })}
                placeholder="예: 치앙마이"
                fullWidth
              />
              <Box display="flex" gap={2}>
                <TextField
                  label="위도"
                  value={cityForm.latitude}
                  onChange={(e) => setCityForm({ ...cityForm, latitude: e.target.value })}
                  placeholder="예: 18.7883"
                  fullWidth
                />
                <TextField
                  label="경도"
                  value={cityForm.longitude}
                  onChange={(e) => setCityForm({ ...cityForm, longitude: e.target.value })}
                  placeholder="예: 98.9853"
                  fullWidth
                />
              </Box>
              <TextField
                label="국가 코드 (ISO 2자리)"
                value={cityForm.countryCode}
                onChange={(e) => setCityForm({ ...cityForm, countryCode: e.target.value })}
                placeholder="예: TH"
                fullWidth
              />
              <TextField
                label="국가명"
                value={cityForm.countryName}
                onChange={(e) => setCityForm({ ...cityForm, countryName: e.target.value })}
                placeholder="예: 태국"
                fullWidth
              />
              <TextField
                select
                label="대륙"
                value={cityForm.continent}
                onChange={(e) => setCityForm({ ...cityForm, continent: e.target.value })}
                fullWidth
              >
                {CONTINENTS.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
              </TextField>
              <TextField
                label="국기 이모지 (선택)"
                value={cityForm.countryEmoji}
                onChange={(e) => setCityForm({ ...cityForm, countryEmoji: e.target.value })}
                placeholder="예: 🇹🇭"
                fullWidth
              />
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddOpen(false)}>취소</Button>
          <Button
            variant="contained"
            onClick={tab === 'country' ? handleAddCountryKeyword : handleAddCityKeyword}
          >
            추가
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default KeywordManagementPanel;
