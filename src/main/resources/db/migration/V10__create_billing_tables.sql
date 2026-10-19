-- ============================================================
-- Billing and Subscription Tables
-- ============================================================

-- Stores the subscription plans available to organizations.
CREATE TABLE plans (
    id UUID PRIMARY KEY, -- Unique identifier for the plan
    name VARCHAR(100) NOT NULL, -- Display name of the plan
    code VARCHAR(50) NOT NULL, -- Unique code used to identify the plan
    description TEXT, -- Optional description of the plan
    price NUMERIC(12, 2) NOT NULL, -- Price of the subscription
    currency VARCHAR(10) NOT NULL, -- Currency of the plan price
    billing_cycle VARCHAR(40) NOT NULL, -- MONTHLY or YEARLY
    active BOOLEAN NOT NULL DEFAULT TRUE, -- Indicates whether the plan is available
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record creation time
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record update time

    -- Prevents duplicate plan codes.
    CONSTRAINT uk_plans_code UNIQUE (code)
);

-- Stores individual features belonging to each subscription plan.
CREATE TABLE plan_features (
    id UUID PRIMARY KEY, -- Unique identifier for the feature
    plan_id UUID NOT NULL, -- Plan to which this feature belongs
    feature_name VARCHAR(100) NOT NULL, -- Name of the feature
    feature_value VARCHAR(255), -- Value or limit associated with the feature
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record creation time
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record update time

    -- Connects the feature to its subscription plan.
    CONSTRAINT fk_plan_features_plan
        FOREIGN KEY (plan_id)
        REFERENCES plans (id),

    -- Prevents duplicate feature names within the same plan.
    CONSTRAINT uk_plan_features_plan_name
        UNIQUE (plan_id, feature_name)
);

-- Stores the current and historical subscriptions of organizations.
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY, -- Unique identifier for the subscription
    organization_id UUID NOT NULL, -- Organization owning the subscription
    plan_id UUID NOT NULL, -- Current subscription plan
    status VARCHAR(40) NOT NULL, -- ACTIVE, CANCELLED, etc.
    billing_cycle VARCHAR(40) NOT NULL, -- MONTHLY or YEARLY
    current_period_start TIMESTAMP WITH TIME ZONE NOT NULL, -- Start of current billing cycle
    current_period_end TIMESTAMP WITH TIME ZONE NOT NULL, -- End of current billing cycle
    started_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Subscription start time
    cancelled_at TIMESTAMP WITH TIME ZONE, -- Cancellation time if cancelled
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record creation time
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record update time

    -- Ensures the subscription belongs to a valid organization.
    CONSTRAINT fk_subscriptions_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations (id),

    -- Connects the subscription to its selected plan.
    CONSTRAINT fk_subscriptions_plan
        FOREIGN KEY (plan_id)
        REFERENCES plans (id)
);

-- Stores invoices generated for organization subscriptions.
CREATE TABLE invoices (
    id UUID PRIMARY KEY, -- Unique identifier for the invoice
    organization_id UUID NOT NULL, -- Organization owning the invoice
    subscription_id UUID, -- Subscription associated with the invoice
    invoice_number VARCHAR(100) NOT NULL, -- Human-readable invoice number
    status VARCHAR(40) NOT NULL, -- PENDING, PAID, FAILED, etc.
    amount NUMERIC(12, 2) NOT NULL, -- Invoice amount
    currency VARCHAR(10) NOT NULL, -- Currency used for the invoice
    invoice_date TIMESTAMP WITH TIME ZONE NOT NULL, -- Date the invoice was generated
    due_date TIMESTAMP WITH TIME ZONE, -- Payment due date
    paid_at TIMESTAMP WITH TIME ZONE, -- Date the invoice was paid
    pdf_data BYTEA, -- Stored PDF content for invoice download
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record creation time
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record update time

    -- Ensures the invoice belongs to a valid organization.
    CONSTRAINT fk_invoices_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations (id),

    -- Connects the invoice to the related subscription.
    CONSTRAINT fk_invoices_subscription
        FOREIGN KEY (subscription_id)
        REFERENCES subscriptions (id),

    -- Prevents duplicate invoice numbers.
    CONSTRAINT uk_invoices_invoice_number UNIQUE (invoice_number)
);

-- Stores usage information for an organization's billing cycle.
CREATE TABLE usage_snapshots (
    id UUID PRIMARY KEY, -- Unique identifier for the usage record
    organization_id UUID NOT NULL, -- Organization owning the usage record
    subscription_id UUID, -- Subscription associated with the usage
    period_start TIMESTAMP WITH TIME ZONE NOT NULL, -- Start of usage period
    period_end TIMESTAMP WITH TIME ZONE NOT NULL, -- End of usage period
    messages_used BIGINT NOT NULL DEFAULT 0, -- Number of messages used
    contacts_used BIGINT NOT NULL DEFAULT 0, -- Number of contacts used
    storage_used BIGINT NOT NULL DEFAULT 0, -- Storage used during the period
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record creation time
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Record update time

    -- Ensures the usage record belongs to a valid organization.
    CONSTRAINT fk_usage_snapshots_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations (id),

    -- Connects the usage record to the related subscription.
    CONSTRAINT fk_usage_snapshots_subscription
        FOREIGN KEY (subscription_id)
        REFERENCES subscriptions (id),

    -- Prevents duplicate usage records for the same organization and period.
    CONSTRAINT uk_usage_snapshots_organization_period
        UNIQUE (organization_id, period_start, period_end)
);

-- Index for quickly finding features belonging to a plan.
CREATE INDEX idx_plan_features_plan_id
    ON plan_features (plan_id);

-- Index for organization-level subscription and status filtering.
CREATE INDEX idx_subscriptions_organization_status
    ON subscriptions (organization_id, status);

-- Index for quickly finding subscriptions by plan.
CREATE INDEX idx_subscriptions_plan_id
    ON subscriptions (plan_id);

-- Index for invoice history filtering by organization and date.
CREATE INDEX idx_invoices_organization_date
    ON invoices (organization_id, invoice_date);

-- Index for filtering invoices by organization and status.
CREATE INDEX idx_invoices_organization_status
    ON invoices (organization_id, status);

-- Index for retrieving usage records by organization and billing period.
CREATE INDEX idx_usage_snapshots_organization_period
    ON usage_snapshots (organization_id, period_start, period_end);