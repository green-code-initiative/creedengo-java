package org.greencodeinitiative.creedengo.java.checks;

class AvoidMultipleIfElseStatementCompareMethodNoIssue {

    public int compare(FieldVo o1, FieldVo o2) {

        if (o1.getIdBlock().equals(o2.getIdBlock())) {
            if (o1.getIdField().equals(o2.getIdField())) {
                return 0;
            }
            // First original
            if (o1.isOriginal() && !o2.isOriginal()) {
                return -1;
            } else if (!o1.isOriginal() && o2.isOriginal()) {
                return 1;
            }
            // First min posgafld
            Long result = o1.getColumnPos() - o2.getColumnPos();
            if (result != 0) {
                return result.intValue();
            }

            // First min ordgaflc
            result = o1.getIndex() - o2.getIndex();
            return result.intValue();
        }
        // First BQRY block
        if (o1.getIdBlock().startsWith("BQRY") && !o2.getIdBlock().startsWith("BQRY")) {
            return -1;
        } else if (!o1.getIdBlock().startsWith("BQRY") && o2.getIdBlock().startsWith("BQRY")) {
            return 1;
        }
        // If both block don't start with BQRY, sort alpha with String.compareTo method
        return o1.getIdBlock().compareTo(o2.getIdBlock());
    }

    public static class FieldVo {

        private String idBlock;

        private String idField;

        private boolean original;

        private long columnPos;

        private long index;

        public String getIdBlock() {
            return idBlock;
        }

        public String getIdField() {
            return idField;
        }

        public boolean isOriginal() {
            return original;
        }

        public long getColumnPos() {
            return columnPos;
        }

        public long getIndex() {
            return index;
        }
    }

}
