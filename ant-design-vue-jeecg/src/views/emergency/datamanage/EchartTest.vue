<template>
  <a-card :bordered="false">
    <a-modal v-model="dwRelationshipModal"
             :title="dwRelationshipModalTitle"
             switchFullscreen
             :width="1400"
             :okButtonProps="{class:{'jee-hidden': true} }"
             cancelText="关闭">
      <a-table bordered
               :scroll="{ x:10000, y: 400 }"
               :columns="factTableColumns"
               :dataSource="factTableData"
               :loading="factTableLoading"
               rowKey="item_id"
               :pagination="paginationOpt">

      </a-table>

    </a-modal>
    <div id="myChart" style="width: 100%; height: 800px">
    </div>
  </a-card>
</template>

<script>
  import {getAction, postAction} from "@/api/manage";
  import * as echarts from 'echarts';
  import { dataManageApi } from '@/api/EmergencyApi.js'
  import _ from 'lodash'

  export default {
    name: 'EchartTest',
    data(){
      return{
        dwRelationshipModalTitle:'',
        dwRelationshipModal:false,
        myChart:null,
        factTableColumns:[],
        factTableData: [],
        factTableLoading:false,
        //数据分页设置
        paginationOpt: {
          defaultCurrent: 1, // 默认当前页数
          defaultPageSize: 20, // 默认当前页显示数据的大小
          total: 0, // 总数，必须先有
          showSizeChanger: true,
          showQuickJumper: true,
          pageSizeOptions: ["5", "10", "15", "20"],
          showTotal: (total) => `共 ${total} 条`, // 显示总数
          onShowSizeChange: (current, pageSize) => {
            this.paginationOpt.defaultCurrent = 1;
            this.paginationOpt.defaultPageSize = pageSize;
          },
          // 改变每页数量时更新显示
          onChange: (current, size) => {
            this.paginationOpt.defaultCurrent = current;
            this.paginationOpt.defaultPageSize = size;
          },
        },
      }
    },
    mounted() {
      this.drawEcharts();
    },
    methods:{
      drawEcharts(){
        let apiUrl = dataManageApi.fetchDimFactRelation
        getAction(apiUrl).then((res)=>{
          if (res.success){
            drawRelationChart(res.result)
            let that = this
            function drawRelationChart(data) {
              let dataPoint = []
              let dataLink = []
              _.forEach(data,function(value) {
                if (value['type']==='fact'){
                  dataPoint.push({
                    name: value['name'],
                    value: value['value'],
                    des: value['des'],
                    type:value['type'],
                    symbolSize: 150,
                    itemStyle: {
                      normal: {
                        color: 'red'
                      }
                    }
                  })
                }
                else if(value['type']==='dim'){
                  dataPoint.push({
                    name: value['name'],
                    value: value['value'],
                    type:value['type'],
                    des: value['des'],
                    symbolSize: 100,
                  })
                }
                else {
                  dataLink.push({
                    source: value['source'],
                    target: value['target'],
                    type:value['type'],
                    name: '',
                    des: '',
                    lineStyle: {
                      normal: {
                        color: '#000',
                      }
                    }
                  })
                }
              })
              let option = {
                title: { text: '数据仓库关系图谱' },
                tooltip: {
                  formatter: function (x) {
                    return x.data.des;
                  }
                },
                series: [
                  {
                    type: 'graph',
                    layout: 'force',
                    symbolSize: 80,
                    roam: true,
                    edgeSymbol: ['circle', 'arrow'],
                    edgeSymbolSize: [4, 10],
                    draggable: true,
                    itemStyle: {
                      normal: {
                        color: '#4b565b'
                      }
                    },
                    lineStyle: {
                      normal: {
                        width: 2,
                        color: '#4b565b'
                      }
                    },
                    edgeLabel: {
                      normal: {
                        show: true,
                        formatter: function (x) {
                          return x.data.name;
                        }
                      }
                    },
                    label: {
                      normal: {
                        show: true,
                        fontSize:12
                      }
                    },
                    force:{
                      repulsion:2000,
                      edgeLength:250
                    },
                    data: dataPoint,
                    links: dataLink
                  }
                ]
              };
              let myChart = echarts.init(document.getElementById('myChart'))
              myChart.setOption(option);
              myChart.on('click',function(params) {
                if (params.data.type==='fact') that.dwModal(params.data)
              })
            }
          }
        })
      },
      dwModal(data){
        this.dwRelationshipModal = true
        this.dwRelationshipModalTitle=data.name;
        let postList = [data.value]
        let apiUrl = dataManageApi.fetchDwData
        postAction(apiUrl,postList).then((res)=>{
          if (res.success){
            let temp_column = []
            temp_column.push({
              title: '序号',
              dataIndex: 'rowIndex',
              width:60,
              align:"center",
              customRender: (text, record, index) =>
                `${(this.paginationOpt.defaultCurrent - 1) *
                this.paginationOpt.defaultPageSize +
                index + 1}`
            })
            let data = res.result
            _.forEach(_.head(data),function(value) {
              temp_column.push({
                title:value.title,
                align:"center",
                dataIndex: value.dataIndex,
                key: value.dataIndex,
              })
            })
            this.factTableColumns = temp_column
            let table_data = _.tail(data)
            let temp_data = []
            let count = 1;
            _.forEach(table_data[0],function(value) {
              value['item_id']= count
              _.forEach(value,function(i,j) {value[j]=String(i);})
              temp_data.push(value)
              count ++;
            })
            this.factTableData = temp_data
          }
        })
      }
    },
  }

</script>

<style>
  #components-popover-demo-placement .ant-btn {
    width: 70px;
    text-align: center;
    padding: 0;
    margin-right: 8px;
    margin-bottom: 8px;
  }
</style>
