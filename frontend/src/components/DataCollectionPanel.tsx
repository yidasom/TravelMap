import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  TextField,
  Alert,
  LinearProgress,
  Grid,
  Divider,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Collapse
} from '@mui/material';
import {
  PlayArrow,
  Stop,
  Refresh,
  Add,
  ExpandMore,
  ExpandLess,
  AdminPanelSettings
} from '@mui/icons-material';
import { dataCollectionApi } from '../services/api';

interface CollectionStatus {
  isCollecting: boolean;
  currentStatus: string;
  processedCount: number;
  totalCount: number;
  progressPercentage: number;
}

interface CollectionResult {
  status: 'success' | 'error';
  message: string;
  processedCount?: number;
  totalCount?: number;
  channelName?: string;
  videoCount?: number;
}

const DataCollectionPanel: React.FC = () => {
  const [status, setStatus] = useState<CollectionStatus>({
    isCollecting: false,
    currentStatus: '대기 중',
    processedCount: 0,
    totalCount: 0,
    progressPercentage: 0
  });
  
  const [result, setResult] = useState<CollectionResult | null>(null);
  const [channelId, setChannelId] = useState('');
  const [channelName, setChannelName] = useState('');
  const [isExpanded, setIsExpanded] = useState(false);
  const [addChannelOpen, setAddChannelOpen] = useState(false);
  
  // 상태 폴링
  useEffect(() => {
    const fetchStatus = async () => {
      try {
        const response = await dataCollectionApi.getCollectionStatus();
        setStatus(response.data);
      } catch (error) {
        console.error('상태 조회 실패:', error);
      }
    };
    
    fetchStatus();
    const interval = setInterval(fetchStatus, 2000); // 2초마다 상태 체크
    
    return () => clearInterval(interval);
  }, []);
  
  const handleCollectAll = async () => {
    try {
      setResult(null);
      const response = await dataCollectionApi.collectAllData();
      setResult(response.data);
    } catch (error: any) {
      setResult({
        status: 'error',
        message: error.response?.data?.message || '전체 데이터 수집 실패'
      });
    }
  };
  
  const handleUpdateAll = async () => {
    try {
      setResult(null);
      const response = await dataCollectionApi.updateAllChannelsData();
      setResult(response.data);
    } catch (error: any) {
      setResult({
        status: 'error',
        message: error.response?.data?.message || '전체 데이터 업데이트 실패'
      });
    }
  };
  
  const handleProcessUnprocessed = async () => {
    try {
      setResult(null);
      const response = await dataCollectionApi.processUnprocessedVideos();
      setResult(response.data);
    } catch (error: any) {
      setResult({
        status: 'error',
        message: error.response?.data?.message || '미처리 영상 처리 실패'
      });
    }
  };
  
  const handleCollectChannel = async () => {
    if (!channelId.trim()) {
      setResult({
        status: 'error',
        message: '채널 ID를 입력해주세요.'
      });
      return;
    }
    
    try {
      setResult(null);
      const response = await dataCollectionApi.collectChannelData(channelId);
      setResult(response.data);
    } catch (error: any) {
      setResult({
        status: 'error',
        message: error.response?.data?.message || '채널 데이터 수집 실패'
      });
    }
  };
  
  const handleAddChannel = async () => {
    if (!channelId.trim()) {
      setResult({
        status: 'error',
        message: '채널 ID를 입력해주세요.'
      });
      return;
    }
    
    try {
      setResult(null);
      const response = await dataCollectionApi.addNewChannel(channelId, channelName);
      setResult(response.data);
      setAddChannelOpen(false);
      setChannelId('');
      setChannelName('');
    } catch (error: any) {
      setResult({
        status: 'error',
        message: error.response?.data?.message || '새 채널 추가 실패'
      });
    }
  };
  
  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <AdminPanelSettings sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6" component="h2">
            데이터 수집 관리
          </Typography>
          <IconButton
            onClick={() => setIsExpanded(!isExpanded)}
            sx={{ ml: 'auto' }}
          >
            {isExpanded ? <ExpandLess /> : <ExpandMore />}
          </IconButton>
        </Box>
        
        {/* 현재 상태 표시 */}
        <Box mb={2}>
          <Box display="flex" alignItems="center" mb={1}>
            <Typography variant="body2" color="text.secondary">
              현재 상태:
            </Typography>
            <Chip
              label={status.isCollecting ? '수집 중' : '대기 중'}
              color={status.isCollecting ? 'warning' : 'default'}
              size="small"
              sx={{ ml: 1 }}
            />
          </Box>
          
          {status.isCollecting && (
            <Box>
              <Typography variant="body2" gutterBottom>
                {status.currentStatus}
              </Typography>
              <LinearProgress
                variant="determinate"
                value={status.progressPercentage}
                sx={{ mb: 1 }}
              />
              <Typography variant="caption" color="text.secondary">
                {status.processedCount} / {status.totalCount} 
                ({status.progressPercentage.toFixed(1)}%)
              </Typography>
            </Box>
          )}
        </Box>
        
        <Collapse in={isExpanded}>
          {/* 결과 메시지 */}
          {result && (
            <Alert 
              severity={result.status === 'success' ? 'success' : 'error'}
              sx={{ mb: 2 }}
              onClose={() => setResult(null)}
            >
              <Typography variant="body2">
                {result.message}
              </Typography>
              {result.processedCount !== undefined && (
                <Typography variant="caption" display="block">
                  처리된 항목: {result.processedCount}
                  {result.totalCount && ` / ${result.totalCount}`}
                </Typography>
              )}
            </Alert>
          )}
          
          {/* 메인 수집 버튼들 */}
          <Grid container spacing={2} mb={2}>
            <Grid item xs={12} sm={6} md={3}>
              <Button
                variant="contained"
                fullWidth
                startIcon={<PlayArrow />}
                onClick={handleCollectAll}
                disabled={status.isCollecting}
                color="primary"
              >
                전체 수집
              </Button>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Button
                variant="outlined"
                fullWidth
                startIcon={<Refresh />}
                onClick={handleUpdateAll}
                disabled={status.isCollecting}
              >
                전체 업데이트
              </Button>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Button
                variant="outlined"
                fullWidth
                startIcon={<PlayArrow />}
                onClick={handleProcessUnprocessed}
                disabled={status.isCollecting}
              >
                미처리 영상
              </Button>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Button
                variant="outlined"
                fullWidth
                startIcon={<Add />}
                onClick={() => setAddChannelOpen(true)}
                disabled={status.isCollecting}
                color="success"
              >
                채널 추가
              </Button>
            </Grid>
          </Grid>
          
          <Divider sx={{ my: 2 }} />
          
          {/* 개별 채널 수집 */}
          <Typography variant="subtitle2" gutterBottom>
            개별 채널 수집
          </Typography>
          <Box display="flex" gap={1} alignItems="center">
            <TextField
              label="채널 ID"
              value={channelId}
              onChange={(e) => setChannelId(e.target.value)}
              placeholder="UC..."
              size="small"
              sx={{ flexGrow: 1 }}
              disabled={status.isCollecting}
            />
            <Button
              variant="contained"
              onClick={handleCollectChannel}
              disabled={status.isCollecting || !channelId.trim()}
              size="small"
            >
              수집
            </Button>
          </Box>
        </Collapse>
        
        {/* 새 채널 추가 다이얼로그 */}
        <Dialog open={addChannelOpen} onClose={() => setAddChannelOpen(false)}>
          <DialogTitle>새 채널 추가</DialogTitle>
          <DialogContent>
            <TextField
              autoFocus
              margin="dense"
              label="채널 ID"
              value={channelId}
              onChange={(e) => setChannelId(e.target.value)}
              placeholder="UC..."
              fullWidth
              variant="outlined"
              required
            />
            <TextField
              margin="dense"
              label="채널 이름 (선택사항)"
              value={channelName}
              onChange={(e) => setChannelName(e.target.value)}
              fullWidth
              variant="outlined"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setAddChannelOpen(false)}>취소</Button>
            <Button 
              onClick={handleAddChannel}
              disabled={!channelId.trim()}
              variant="contained"
            >
              추가
            </Button>
          </DialogActions>
        </Dialog>
      </CardContent>
    </Card>
  );
};

export default DataCollectionPanel; 