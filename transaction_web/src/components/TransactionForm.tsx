import React from 'react';
import { Form, Input, Select, DatePicker, Button, InputNumber } from 'antd';
import { Transaction, TransactionFormData } from '../types/transaction';
import moment from 'moment';
import '../custom_antd.css';

const { Option } = Select;

interface TransactionFormProps {
  initialValues?: Transaction;
  onSubmit: (values: TransactionFormData) => void;
  onCancel: () => void;
}

const TransactionForm: React.FC<TransactionFormProps> = ({
  initialValues,
  onSubmit,
  onCancel,
}) => {
  const [form] = Form.useForm();
  const [selectedType, setSelectedType] = React.useState<string | undefined>(initialValues?.type); 

  const handleSubmit = (values: any) => {
    const formData: TransactionFormData = {
      ...values,
      currency:"CNY",
      // transactionDate: values.transactionDate.format('YYYY-MM-DD'),
    };
    onSubmit(formData);
  };

  React.useEffect(() => {
    if (initialValues) {
      form.setFieldsValue({
        ...initialValues,
        timeStamp: moment(initialValues.timestamp),
      });
    }
  }, [initialValues, form]);

  const handleTypeChange = (value: string) => {
    setSelectedType(value);
  };

  return (
    <Form
      form={form}
      layout="horizontal"
      onFinish={handleSubmit}
      initialValues={initialValues}
      labelCol={{ span: 4 }} 
      wrapperCol={{ span: 20 }}
    >
      <Form.Item
        name="amount"
        label="Amount"
        rules={[{ required: true, message: 'Please input amount!' }]}
      >
        <InputNumber style={{ width: '100%' }} />
      </Form.Item>

      <Form.Item
        name="type"
        label="Type"
        rules={[{ required: true, message: 'Please select type!' }]}
      >
        <Select onChange={handleTypeChange} value={selectedType}>
          <Option value="DEPOSIT">Deposit</Option>
          <Option value="WITHDRAWAL">Withdrawal</Option>
        </Select>
      </Form.Item>      

      <Form.Item
        name="riskLevel"
        label="RiskLevel"
        rules={[{ required: true, message: 'Please select risk level!' }]}
      >
        <Select>
          <Option value="LOW">Low</Option>
          <Option value="MEDIUM">Medium</Option>
          <Option value="HIGH">Critical</Option>
          <Option value="CRITICAL">CRITICAL</Option>
        </Select>
      </Form.Item>

      <Form.Item
        name="status"
        label="Status"
        rules={[{ required: true, message: 'Please select status!' }]}
      >
        <Select>
          <Option value="INITIATED">Initiated</Option>
          <Option value="PENDING">Pending</Option>
          <Option value="PROCESSING">Processing</Option>
          <Option value="COMPLETED">Completed</Option>
          <Option value="FAILED">Failed</Option>
          <Option value="CANCELLED">Canceled</Option>
          <Option value="REVERSED">Reversed</Option>
          <Option value="REJECTED">Rejected</Option>
          <Option value="SUSPICIOUS">Suspicious</Option>
        </Select>
      </Form.Item>

      <Form.Item
        name="sourceAccountNumber"
        label="Source"
        rules={[{ required: true, message: 'Please input source!' }]}
      >
        <Input />
      </Form.Item>
      {selectedType === 'TRANSFER' && (
        <Form.Item
          name="destinationAccountNumber"
          label="Destination:"
        >
          <Input />
        </Form.Item>
      )}

      <Form.Item
        name="category"
        label="Category"
        rules={[{ required: true, message: 'Please input category!' }]}
      >
        <Select>
          <Option value="SALARY">Salary</Option>
          <Option value="INVESTMENT">Investment</Option>
          <Option value="SHOPPING">Shopping</Option>
          <Option value="UTILITIES">Utilities</Option>
          <Option value="ENTERTAINMENT">Entertainment</Option>
          <Option value="TRANSFER">Transfer</Option>
          <Option value="LOAN_PAYMENT">Loan Payment</Option>
          <Option value="INSURANCE">Insureance</Option>
          <Option value="TAX">Tax</Option>
          <Option value="OTHER">Other</Option>
        </Select>
      </Form.Item>

      {initialValues && (
            <Form.Item
                name="timestamp"
                style={{ display: 'none' }}
                rules={[]}
            >
                <Input />
            </Form.Item>
        )}

      <Form.Item name="remarks" label="Remarks">
        <Input.TextArea />
      </Form.Item>

      <Form.Item>
        <Button type="primary" htmlType="submit" style={{ marginRight: 8 }}>
          Submit
        </Button>
        <Button onClick={onCancel}>Cancel</Button>
      </Form.Item>
    </Form>
  );
};

export default TransactionForm; 